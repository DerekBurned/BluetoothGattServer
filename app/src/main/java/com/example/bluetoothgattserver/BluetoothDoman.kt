package com.example.bluetoothgattserver
import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable

class BluetoothDoman(val name: String, val device: BluetoothDevice) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BluetoothDoman
        return device.address == other.device.address
    }

    override fun hashCode(): Int {
        return device.address.hashCode()
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeParcelable(device, flags)
    }

    companion object CREATOR : Parcelable.Creator<BluetoothDoman> {
        override fun createFromParcel(parcel: Parcel): BluetoothDoman {
            val name = parcel.readString() ?: ""
            val device = parcel.readParcelable<BluetoothDevice>(BluetoothDevice::class.java.classLoader)
                ?: throw IllegalArgumentException("BluetoothDevice is missing")
            return BluetoothDoman(name, device)
        }

        override fun newArray(size: Int): Array<BluetoothDoman?> {
            return arrayOfNulls(size)
        }
    }
}
