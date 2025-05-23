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
class GattServerController(private val context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var gattServer: BluetoothGattServer? = null
    private val connectedDevices = mutableMapOf<String, BluetoothDevice>()
    private val bleAdvertiser = BleAdvertiser(context)

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("dc3bd49f-3a1a-4972-abd6-f3692a896a36")
        val CHARACTERISTIC_UUID: UUID = UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB")
        val DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
        const val MAX_MTU = 247
    }



    private var listener: GattServerListener? = null

    fun setListener(listener: GattServerListener) {
        this.listener = listener
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevices[device.address] = device
                    Log.d("GattServer", "Device connected: ${device.address}")
                    listener?.onDeviceConnected(device)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevices.remove(device.address)
                    Log.d("GattServer", "Device disconnected: ${device.address}")
                    listener?.onDeviceDisconnected(device)
                }
            }
        }

        override fun onMtuChanged(device: BluetoothDevice, mtu: Int) {
            Log.d("GattServer", "MTU changed to $mtu for ${device.address}")
            listener?.onMtuChanged(device, mtu)
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.value)
            Log.d("GattServer", "Read request from ${device.address}")
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            if (responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }
            Log.d("GattServer", "Write request from ${device.address}: ${value.decodeToString()}")
            listener?.onDataReceived(device, value)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice, requestId: Int, descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            if (descriptor.uuid == DESCRIPTOR_UUID && responseNeeded) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                Log.d("GattServer", "Descriptor written by ${device.address}")
            }
        }

        override fun onNotificationSent(device: BluetoothDevice, status: Int) {
            Log.d("GattServer", "Notification sent to ${device.address}, status: $status")
            listener?.onNotificationSent(device, status)
        }
    }

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

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val characteristic = BluetoothGattCharacteristic(
            CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        val descriptor = BluetoothGattDescriptor(DESCRIPTOR_UUID, BluetoothGattDescriptor.PERMISSION_WRITE)
        characteristic.addDescriptor(descriptor)
        service.addCharacteristic(characteristic)

        val success = gattServer?.addService(service) == true
        if (success) {
            Log.d("GattServer", "GATT server started successfully")
            bleAdvertiser.startAdvertising()
        } else {
            Log.e("GattServer", "Failed to add GATT service")
        }
        return success
    }

    fun stopServer() {
        bleAdvertiser.stopAdvertising()
        connectedDevices.clear()
        gattServer?.close()
        gattServer = null
        Log.d("GattServer", "GATT server stopped")
    }

    fun notifyAllDevices(data: ByteArray): Map<String, Boolean> {
        val result = mutableMapOf<String, Boolean>()
        val characteristic = gattServer?.getService(SERVICE_UUID)?.getCharacteristic(CHARACTERISTIC_UUID)
            ?: return result
        characteristic.value = data

        connectedDevices.values.forEach { device ->
            result[device.address] = gattServer?.notifyCharacteristicChanged(device, characteristic, false) ?: false
        }
        return result
    }

    fun notifyDevice(deviceAddress: String, data: ByteArray): Boolean {
        val device = connectedDevices[deviceAddress] ?: return false
        val characteristic = gattServer?.getService(SERVICE_UUID)?.getCharacteristic(CHARACTERISTIC_UUID) ?: return false
        characteristic.value = data
        Log.d("Gatt Server message ", "Message sent to device adress: $deviceAddress, message: ${data.decodeToString()}")
        return gattServer?.notifyCharacteristicChanged(device, characteristic, false) ?: false
    }

    fun getConnectedDevices(): List<BluetoothDevice> = connectedDevices.values.toList()

    fun isDeviceConnected(address: String): Boolean = connectedDevices.containsKey(address)

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    }
}
