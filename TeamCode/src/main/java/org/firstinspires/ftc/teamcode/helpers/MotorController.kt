package org.firstinspires.ftc.teamcode.helpers

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import java.util.*
import kotlin.collections.HashSet
import kotlin.math.abs

abstract class MotorController @JvmOverloads constructor(
    override val opMode: OpMode,
    protected val motors: MutableCollection<DcMotorInfo> = HashSet(),
    @JvmField var powerMultiplier: Float = 1f,
    @JvmField var brake: Boolean? = true,
    var defaultPower: Float = 0f
) : DcMotorInfoOpModeHelper {

    init {
        motors.forEach {
            it.brake = brake
        }
    }

    protected val hardwareMap: HardwareMap by lazy { opMode.hardwareMap } // TODO: is this what "by lazy" is for?
    private val telemetry: Telemetry by lazy { opMode.telemetry }

    open var power = 0f

    protected fun <T> output(data: T) {
        val motorsControlling: String = when (motors.size) {
            0 -> "no motors yet"
            1 -> "the motor ${motors.joinToString { it.name }}"
            else -> "the motors ${motors.joinToString { it.name }}"
        }
        telemetry.addData("[From MotorController of $motorsControlling]", data)
    }

    protected fun <T> warn(warning: T?) {
        output("Warning: $warning")
    }

    fun addMotor(name: String, power: Float = 1f) {
        addMotor(DcMotorInfo.fromName(name, power))
    }

    fun addMotor(motor: DcMotor, power: Float = 1f) {
        addMotor(DcMotorInfo.fromMotor(motor, power))
    }

    fun addMotor(motor: DcMotorInfo) {
        motors += motor
    }

    fun addMotors(motors: HashSet<DcMotorInfo>) {
        this.motors += motors
    }

    protected inline fun <T> setAll(value: T, with: (motor: DcMotorInfo, value: T) -> Unit) {
        motors.forEach { motor ->
            with(motor, value)
        }
    }

    open fun stop() {
        power = 0f
    }

    open fun go() {
        power = defaultPower
    }
}

abstract class NonPositionalMotorController @JvmOverloads constructor(
    opMode: OpMode,
    motors: MutableCollection<DcMotorInfo> = HashSet(),
    powerMultiplier: Float = 1f,
    brake: Boolean? = true,
    defaultPower: Float = 0f
) : MotorController(opMode, motors, powerMultiplier, brake, defaultPower), Waitable {

    private var privateDirection: Direction = Direction.STOP

    var direction: Direction
        set(value) {
            power *= value.multiplier
        }
        get() = privateDirection

    /**
     * Stores the current unsigned power of all of the motors
     * together. Although this may not be the actual power
     * of any given individual motor, their unsigned powers
     * will all be equal to this value multiplied by their
     * individual power multipliers as specified in their
     * DcMotorInfo objects. Updating this property is the
     * preferred way to change the power of the motors.
     *
     * Note that, as an unsigned value, this property only stores
     * the magnitude of the power, and not the direction. Upon
     * setting this property with a non-positive value, the
     * {@link #privateDirection()} will be updated appropriately
     * and the power will be set to the absolute value of
     * the value.
     */
    override var power = 0f
        set(value) {
            if (value != 0f) field = abs(value)
            privateDirection = when {
                value > 0f -> Direction.FORWARDS
                value < 0f -> Direction.BACKWARDS
                else       -> Direction.STOP // if zero
            }
            setAll(value = value * powerMultiplier, with = ::moveMotor)
        }

    override fun stop() {
        direction = Direction.STOP
    }

    abstract fun moveMotor(motor: DcMotorInfo, power: Float)

    fun move(power: Float = defaultPower, direction: Direction? = null, seconds: Double? = null) {
        this.power = power with direction

        if (seconds != null) {
            waitSeconds(seconds)
            this.stop()
        }
    }
}

open class PowerBasedNonPositionalMotorController @JvmOverloads constructor(
    opMode: OpMode,
    motors: MutableCollection<DcMotorInfo> = HashSet(),
    powerMultiplier: Float = 1f,
    brake: Boolean? = true,
    defaultPower: Float = 0f
) : NonPositionalMotorController(opMode, motors, powerMultiplier, brake, defaultPower) {

    override fun moveMotor(motor: DcMotorInfo, power: Float) {
        motor.encoderMode = DcMotor.RunMode.RUN_USING_ENCODER
        motor.runPower = power
    }
}

open class SpeedBasedNonPositionalMotorController @JvmOverloads constructor(
    opMode: OpMode,
    motors: MutableCollection<DcMotorInfo> = HashSet(),
    powerMultiplier: Float = 1f,
    brake: Boolean? = true,
    defaultPower: Float = 0f
) : NonPositionalMotorController(opMode, motors, powerMultiplier, brake, defaultPower) {

    override fun moveMotor(motor: DcMotorInfo, power: Float) {
        motor.encoderMode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        motor.runPower = power
    }
}

class WheelList (
    var frontLeft: DcMotorInfo,
    var frontRight: DcMotorInfo,
    var backLeft: DcMotorInfo,
    var backRight: DcMotorInfo) : AbstractMutableList<DcMotorInfo>(), MutableCollection<DcMotorInfo> {

    override val size: Int = 4

    private val indexToWheelReferenceMap = arrayListOf(
        ::frontLeft, ::frontRight, ::backLeft, ::backRight
    ) // not actually a map

    override fun get(index: Int): DcMotorInfo =
        indexToWheelReferenceMap[index].get()

    override fun set(index: Int, element: DcMotorInfo): DcMotorInfo {
        indexToWheelReferenceMap[index].apply {
            set(element)
            return@set get()
        }
    }

    override fun removeAt(index: Int): Nothing =
        throw NotImplementedError("WheelList cannot contain less than four wheels")

    override fun add(index: Int, element: DcMotorInfo): Nothing =
        throw NotImplementedError("Cannot add more than four wheels to WheelList")
}

internal interface Driveable : Waitable {
    val rotationSpeed: Float
    val wheelList: WheelList

    fun drive(forward: Float = 0f, right: Float = 0f, clockwise: Float = 0f) {

        var frontLeft = forward + right + rotationSpeed * clockwise
        var frontRight = forward - right - rotationSpeed * clockwise
        var backLeft = forward - right + rotationSpeed * clockwise
        var backRight = forward + right - rotationSpeed * clockwise

        val max = Collections.max(
            listOf(frontLeft, frontRight, backLeft, backRight)
                .map(::abs)
        ) // gets the largest absolute value of the direction powers

        if (max > 1) {
            frontLeft /= max
            frontRight /= max
            backLeft /= max
            backRight /= max
        }

        wheelList.run {
            this.frontLeft.runPower = frontLeft
            this.frontRight.runPower = frontRight
            this.backRight.runPower = backRight
            this.backLeft.runPower = backLeft
        }
    }

    fun stopDriving() = drive(0f, 0f, 0f)

    fun driveSeconds(seconds: Double,
                     forward: Float = 0f, right: Float = 0f, clockwise: Float = 0f) {

        drive(forward, right, clockwise)
        waitSeconds(seconds)
        stopDriving()
    }
}

class DriveablePowerBasedNonPositionalMotorController @JvmOverloads constructor(
    opMode: OpMode,
    override val wheelList: WheelList,
    powerMultiplier: Float = 1f,
    @JvmField override var rotationSpeed: Float = 1f,
    brake: Boolean? = true,
    defaultPower: Float = 0f
) : PowerBasedNonPositionalMotorController(opMode, wheelList, powerMultiplier, brake, defaultPower),
    Driveable

class DriveableSpeedBasedNonPositionalMotorController @JvmOverloads constructor(
    opMode: OpMode,
    override val wheelList: WheelList,
    powerMultiplier: Float = 1f,
    @JvmField override var rotationSpeed: Float = 1f,
    brake: Boolean? = true,
    defaultPower: Float = 0f
) : SpeedBasedNonPositionalMotorController(opMode, wheelList, powerMultiplier, brake, defaultPower),
    Driveable


class PositionalMotorController @JvmOverloads constructor(
    opMode: OpMode,
    motors: MutableCollection<DcMotorInfo> = HashSet(),
    powerMultiplier: Float = 1f,
    brake: Boolean? = true,
    defaultPower: Float = 0f,
    encoderMode: DcMotor.RunMode = DcMotor.RunMode.RUN_USING_ENCODER
) : MotorController(opMode, motors, powerMultiplier, brake, defaultPower) {

    var targetPosition = 0
        set(value) {
            field = value
            setAll(value) { motor, targetPosition ->
                motor.encoderMode = DcMotor.RunMode.RUN_TO_POSITION
                motor.targetPosition = targetPosition
            }
        }

    override var power = 0f
        set(value) {
            field = value
            setAll(value) { motor, power ->
                motor.encoderMode = DcMotor.RunMode.RUN_TO_POSITION
                motor.runPower = power * powerMultiplier
            }
        }
}
