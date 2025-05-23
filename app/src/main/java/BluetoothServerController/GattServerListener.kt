package BluetoothServerController

import android.bluetooth.BluetoothDevice

 interface GattServerListener {
    fun onDeviceConnected(device: BluetoothDevice)
    fun onDeviceDisconnected(device: BluetoothDevice)
    fun onDataReceived(device: BluetoothDevice, data: ByteArray)
    fun onMtuChanged(device: BluetoothDevice, mtu: Int)
    fun onNotificationSent(device: BluetoothDevice, status: Int)
}