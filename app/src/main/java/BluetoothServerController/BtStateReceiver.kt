package BluetoothServerController

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class BtStateReceiver(private val listener: BluetoothStateListener) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                listener.onBluetoothTurnedOff()
                }
                BluetoothAdapter.STATE_ON -> {
                listener.onBluetoothTurnedOn()
                }
            }
        }
    }
}