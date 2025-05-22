package BluetoothServerController

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.*

@SuppressLint("MissingPermission")
class GattServerController(private val context: Context ){
    private var bluetoothManager: BluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private var gattServer: BluetoothGattServer? = null
    private val connectedDevices = mutableMapOf<String, BluetoothDevice>()

    // Define your service and characteristic UUIDs
    companion object {
        val SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")
        val CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
        val DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
        const val MAX_MTU = 247 // Maximum supported MTU size
    }

    interface GattServerListener {
        fun onDeviceConnected(device: BluetoothDevice)
        fun onDeviceDisconnected(device: BluetoothDevice)
        fun onDataReceived(device: BluetoothDevice, data: ByteArray)
        fun onMtuChanged(device: BluetoothDevice, mtu: Int)
        fun onNotificationSent(device: BluetoothDevice, status: Int)
    }

    private var listener: GattServerListener? = null

    fun setListener(listener: GattServerListener) {
        this.listener = listener
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevices[device.address] = device
                    Log.d("GattServer", "Device connected: ${device.name} (${device.address})")
                    listener?.onDeviceConnected(device)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevices.remove(device.address)
                    Log.d("GattServer", "Device disconnected: ${device.name} (${device.address})")
                    listener?.onDeviceDisconnected(device)
                }
            }
        }

        override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Log.d("GattServer", "MTU changed to $mtu for ${device.name}")
            listener?.onMtuChanged(device, mtu)
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            gattServer?.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                offset,
                characteristic.value
            )
            Log.d("GattServer", "Read request from ${device.name}")
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )

            if (responseNeeded) {
                gattServer?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    offset,
                    null
                )
            }

            Log.d("GattServer", "Received data from ${device.name}: ${value.toString(Charsets.UTF_8)}")
            listener?.onDataReceived(device, value)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )

            if (descriptor.uuid == DESCRIPTOR_UUID) {
                if (responseNeeded) {
                    gattServer?.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        offset,
                        null
                    )
                }
                Log.d("GattServer", "Descriptor write from ${device.name}")
            }
        }

        override fun onNotificationSent(device: BluetoothDevice, status: Int) {
            super.onNotificationSent(device, status)
            Log.d("GattServer", "Notification sent to ${device.name}, status: $status")
            listener?.onNotificationSent(device, status)
        }
    }

    @SuppressLint("MissingPermission")
    fun startServer(): Boolean {
        if (!checkPermissions()) {
            Log.e("GattServer", "Bluetooth permissions not granted")
            return false
        }

        if (bluetoothAdapter?.isEnabled != true) {
            Log.e("GattServer", "Bluetooth is not enabled")
            return false
        }

        gattServer = bluetoothManager.openGattServer(context, gattServerCallback) ?: run {
            Log.e("GattServer", "Failed to open GATT server")
            return false
        }

        // Create service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Create characteristic with read/write/notify properties
        val characteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                    BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or
                    BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // Create descriptor for notifications
        val descriptor = BluetoothGattDescriptor(
            DESCRIPTOR_UUID,
            BluetoothGattDescriptor.PERMISSION_WRITE
        )
        characteristic.addDescriptor(descriptor)
        service.addCharacteristic(characteristic)

        // Add service to server
        return if (gattServer?.addService(service) == true) {
            Log.d("GattServer", "GATT server started successfully")
            true
        } else {
            Log.e("GattServer", "Failed to add GATT service")
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun stopServer() {
        connectedDevices.clear()
        gattServer?.close()
        gattServer = null
        Log.d("GattServer", "GATT server stopped")
    }

    @SuppressLint("MissingPermission")
    fun notifyAllDevices(data: ByteArray): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        if (!validateService()) {
            Log.e("GattServer", "Service not available for notifications")
            return results
        }

        val service = gattServer?.getService(SERVICE_UUID) ?: return results
        val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID) ?: return results

        characteristic.value = data
        connectedDevices.values.forEach { device ->
            results[device.address] = gattServer?.notifyCharacteristicChanged(device, characteristic, false) ?: false
        }
        return results
    }

    @SuppressLint("MissingPermission")
    fun notifyDevice(deviceAddress: String, data: ByteArray): Boolean {
        val device = connectedDevices[deviceAddress] ?: return false
        if (!validateService()) return false

        val service = gattServer?.getService(SERVICE_UUID) ?: return false
        val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID) ?: return false

        characteristic.value = data
        return gattServer?.notifyCharacteristicChanged(device, characteristic, false) ?: false
    }

    fun getConnectedDevices(): List<BluetoothDevice> {
        return connectedDevices.values.toList()
    }

    fun isDeviceConnected(deviceAddress: String): Boolean {
        return connectedDevices.containsKey(deviceAddress)
    }

    private fun validateService(): Boolean {
        return gattServer?.getService(SERVICE_UUID)?.getCharacteristic(CHARACTERISTIC_UUID) != null
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) == PackageManager.PERMISSION_GRANTED
    }
    /*@SuppressLint("MissingPermission")
    fun sendDataToDevice(deviceAddress: String, data: ByteArray): Boolean {
        // 1. Get the target device
        val device = connectedDevices[deviceAddress] ?: return false

        // 2. Get the service and characteristic
        val service = gattServer?.getService(SERVICE_UUID) ?: return false
        val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID) ?: return false

        // 3. Set the data value
        characteristic.value = data

        // 4. Send notification
        return gattServer?.notifyCharacteristicChanged(device, characteristic, false) ?: false
    }*/
    @SuppressLint("MissingPermission")
    fun broadcastData(data: ByteArray): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()

        val service = gattServer?.getService(SERVICE_UUID) ?: return emptyMap()
        val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID) ?: return emptyMap()

        characteristic.value = data

        connectedDevices.forEach { (address, device) ->
            results[address] = gattServer?.notifyCharacteristicChanged(device, characteristic, false) ?: false
        }

        return results
    }
}