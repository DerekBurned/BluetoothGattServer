package BluetoothServerController

interface BluetoothStateListener {
    fun onBluetoothTurnedOff()
    fun onBluetoothTurnedOn()
}
