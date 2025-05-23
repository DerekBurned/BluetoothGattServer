// SharedDevicesViewModel.kt
import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class SharedDevicesViewModel(application: Application) : AndroidViewModel(application) {
    private val _connectedDevices = MutableLiveData<List<Pair<String, BluetoothDevice>>>()
    val connectedDevices: LiveData<List<Pair<String, BluetoothDevice>>> = _connectedDevices

    fun updateDevices(devices: List<Pair<String, BluetoothDevice>>) {
        _connectedDevices.value = devices
    }
}