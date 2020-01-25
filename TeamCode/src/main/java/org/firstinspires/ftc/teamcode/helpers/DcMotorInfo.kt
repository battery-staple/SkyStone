package org.firstinspires.ftc.teamcode.helpers

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotor.ZeroPowerBehavior
import com.qualcomm.robotcore.hardware.HardwareMap

/**
 * A simple data class to encapsulate information
 * and facilitate changing attributes of a [DcMotor].
 *
 * This class is particularly useful when a [DcMotor]
 * must be constructed or its name in the configuration
 * file must otherwise be accessed. It also stores a
 * [powerMultiplier], which indicates that this motor's
 * are "scaled" by a certain factor; this is useful for
 * running several motors at proportional speeds; for
 * example when two motors must run in opposite directions
 * or when they have different gear ratios yet must
 * maintain in sync.
 *
 * @constructor Creates a new DcMotorInfo object of
 * a motor, its name, and its proportional power
 * @property motor the motor this object refers to and controls
 * @property powerMultiplier the scaling factor of any
 * power the motor is set to.
 * @property name the name of the motor, as set in the
 * configuration file.
 * @author Rohen Giralt
 * @sample MotorController // TODO is this how you use sample in KDoc?
 */
data class DcMotorInfo(@JvmField var motor: DcMotor,
                       @JvmField var powerMultiplier: Float = 1f,
                       @JvmField var name: String) { // TODO: Make more general (DeviceInfo superclass)

    /**
     * Stores the current power of the motor.
     *
     * Although very similar, [runPower] has a few
     * slight differences to [motor.power]; for one,
     * it is stored as a Float rather than a Double.
     * This is simply for interoperability and
     * irrelevant to functionality, as both the loss
     * in precision and gain in memory are completely
     * negligible in practice.
     * In addition, setting [runPower] actually sets
     * the motor's power to the value set multiplied
     * by this [DcMotorInfo]'s [powerMultiplier]; this
     * facilitates usage of powerMultiplier without it
     * needing to be typed every time.
     *
     * Note that setting this value does set the
     * motor's power, likely causing it to run.
     *
     * @author Rohen Giralt
     * @see DcMotor.getPower
     * @see DcMotor.setPower
     * @see motor.power
     */
    var runPower: Float
        get() = motor.power.toFloat()
        set(value) {
            motor.power = value.toDouble() * powerMultiplier
        }


    /**
     * Stores the motor's [ZeroPowerBehavior] in a simpler way.
     *
     * Since zeroPowerBehavior essentially only enables and
     * disables the braking of the motor on halt, it makes
     * sense to represent it as a Boolean, where true
     * represents [ZeroPowerBehavior.BRAKE] and false
     * represents [ZeroPowerBehavior.FLOAT].
     *
     * [brake] is nullable to allow representation of
     * [ZeroPowerBehavior.UNKNOWN], which, although likely
     * never used, is still a valid enum value and was included
     * in the off chance it may need to be accessed through
     * the [DcMotorInfo] object. In general, however, [brake]
     * should almost always be nonnull.
     *
     * @author Rohen Giralt
     * @see DcMotor.ZeroPowerBehavior
     * @see motor.zeroPowerBehavior
     */
    var brake: Boolean?
        get() = when (motor.zeroPowerBehavior?: ZeroPowerBehavior.UNKNOWN) {
            ZeroPowerBehavior.BRAKE -> true
            ZeroPowerBehavior.FLOAT -> false
            ZeroPowerBehavior.UNKNOWN -> null
        }
        set(value) {
            motor.zeroPowerBehavior = when (value) {
                true -> ZeroPowerBehavior.BRAKE
                false -> ZeroPowerBehavior.FLOAT
                null -> ZeroPowerBehavior.UNKNOWN
            }
        }

    init {
        brake = true
    }

    /**
     * Stores the motor's current [DcMotor.RunMode].
     *
     * [encoderMode] is merely a proxy for [motor.mode];
     * it adds no new functionality and works exactly
     * the same as using [motor.mode] - it's merely more
     * convenient and consistent to access it through the
     * [DcMotorInfo] object.
     *
     * @author Rohen Giralt
     * @see DcMotor.RunMode
     * @see DcMotor.setMode
     * @see DcMotor.getMode
     */
    var encoderMode: DcMotor.RunMode
        get() = motor.mode
        set(value) { motor.mode = value }

    /**
     * Stores the motor's current targetPosition.
     *
     * [targetPosition] is merely a proxy for
     * [motor.targetPosition]; it adds no new functionality
     * and works exactly the same as using [motor.targetPosition]
     * - it's merely more convenient and consistent to access
     * it through the [DcMotorInfo] object.
     *
     * @author Rohen Giralt
     * @see DcMotor.getTargetPosition
     * @see DcMotor.setTargetPosition
     */
    var targetPosition: Int
        get() = motor.targetPosition
        set(value) { motor.targetPosition = value }

    /**
     * Contains various factory methods for facilitating
     * [DcMotorInfo] construction.
     *
     * @author Rohen Giralt
     * @see OpModeHelpers.DcMotorInfoHelper
     */
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
         *
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
