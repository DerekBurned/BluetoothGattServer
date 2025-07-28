package BluetoothServerController

import android.annotation.SuppressLint
import android.content.Context

object GattServerManager {
    @SuppressLint("StaticFieldLeak")
    private var gattServerController: GattServerController? = null

    fun initialize(context: Context) {
        if (gattServerController == null) {
            gattServerController = GattServerController(context)
        }
    }

    fun getController(): GattServerController? {
        return gattServerController
    }
    fun bluetoothEnabled() : Boolean{
        return gattServerController?.bluetoothEnabled()!!
    }

    fun isInitialized(): Boolean = gattServerController != null

    fun stopServer() {
        gattServerController?.stopServer()
        gattServerController = null
    }
}
