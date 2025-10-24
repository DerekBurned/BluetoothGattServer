package com.example.bluetoothgattserver
import android.bluetooth.BluetoothDevice
import android.os.Parcel
import android.os.Parcelable

class BluetoothDomain(val name: String, val device: BluetoothDevice) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BluetoothDomain
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

    companion object CREATOR : Parcelable.Creator<BluetoothDomain> {
        override fun createFromParcel(parcel: Parcel): BluetoothDomain {
            val name = parcel.readString() ?: ""
            val device = parcel.readParcelable<BluetoothDevice>(BluetoothDevice::class.java.classLoader)
                ?: throw IllegalArgumentException("BluetoothDevice is missing")
            return BluetoothDomain(name, device)
        }

        override fun newArray(size: Int): Array<BluetoothDomain?> {
            return arrayOfNulls(size)
        }
    }
}
