package org.firstinspires.ftc.teamcode.helpers

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import java.lang.IllegalArgumentException

//internal fun <K,V> HashMap<K, V>.getFirstKeyFromValue(value: V): K {
//    for ((k, v) in this) {
//        if (v == value) {
//            return k
//        }
//    }
//}
// Use HardwareMap.getNamesOf() instead

data class DcMotorInfo(@JvmField var motor: DcMotor,
                       @JvmField var powerMultiplier: Float = 1f,
                       @JvmField var name: String) { // TODO: Make more general (DeviceInfo superclass)

    var runPower: Float = motor.power.toFloat()
        get() = motor.power.toFloat()
        set(value) {
            field = value
            motor.power = field.toDouble() * powerMultiplier
        }

    var brake: Boolean? = true
        get() = when (motor.zeroPowerBehavior?: DcMotor.ZeroPowerBehavior.UNKNOWN) {
            DcMotor.ZeroPowerBehavior.BRAKE -> true
            DcMotor.ZeroPowerBehavior.FLOAT -> false
            DcMotor.ZeroPowerBehavior.UNKNOWN -> null
        }
        set(value) {
            field = value
            motor.zeroPowerBehavior = when (field) {
                true -> DcMotor.ZeroPowerBehavior.BRAKE
                false -> DcMotor.ZeroPowerBehavior.FLOAT
                null -> DcMotor.ZeroPowerBehavior.UNKNOWN
            }
        }

    var encoderMode: DcMotor.RunMode
        get() = motor.mode
        set(value) { motor.mode = value /* depending on how long things take, this may be a superior implementation:

        if (this.motor.mode != value) {
            this.motor.mode = value
        }
        */}

    var targetPosition
        get() = motor.targetPosition
        set(value) { motor.targetPosition = value }

    companion object Factory {
        /**
         * Constructs a new DcMotorInfo object with
         * the name generated automatically from the motor
         * through the hardwareMap.
         *
         * There may be more than one element
         * in set returned from hardwareMap,
         * but nothing to handle it, although that
         * <em>should</em> never occur.
         */
        @JvmStatic
        @JvmOverloads
        fun fromMotor(hardwareMap: HardwareMap, motor: DcMotor, power: Float = 1f): DcMotorInfo {
            if (hardwareMap.getNamesOf(motor).isNotEmpty()) {
                return DcMotorInfo(motor, power, hardwareMap.getNamesOf(motor).elementAt(0))
            } else {
                throw IllegalArgumentException("No motor found in hardwareMap")
            }
        }

        /**
         * Constructs a new DcMotorInfo object with
         * the motor gotten automatically from the name
         * through the hardwareMap.
         */
        @JvmStatic
        @JvmOverloads
        fun fromName(hardwareMap: HardwareMap, motorName: String, power: Float = 1f): DcMotorInfo {
            return DcMotorInfo(hardwareMap.get(DcMotor::class.java, motorName), power, motorName)
        }
    }
}
