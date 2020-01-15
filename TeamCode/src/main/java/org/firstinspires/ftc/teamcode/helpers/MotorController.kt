//package org.firstinspires.ftc.teamcode.helpers
//
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import com.qualcomm.robotcore.hardware.DcMotor
//import com.qualcomm.robotcore.hardware.HardwareMap
//import org.firstinspires.ftc.robotcore.external.Telemetry
//import org.firstinspires.ftc.teamcode.ChildDrive
//import kotlin.math.abs
//
//abstract class MotorController @JvmOverloads constructor(
//    override val opMode: OpMode,
//    protected val motors: HashSet<DcMotorInfo>,
//    protected var powerMultiplier: Float = 1f,
//    @JvmField var brake: Boolean? = true) : DcMotorInfoOpModeHelpers {
//
//    init {
//        motors.forEach {
//            it.brake = brake
//        }
//    }
//
//    protected val hardwareMap: HardwareMap by lazy { opMode.hardwareMap } // TODO: is this what "by lazy" is for?
//    protected val telemetry: Telemetry by lazy { opMode.telemetry }
//
//    open var currentPower = 0f
//    var defaultPower = 0f
//
//    protected fun <T> output(data: T) {
//        val motorsControlling: String = when (motors.size) {
//            0 -> "no motors yet" // TODO: should I use motors.isEmpty?
//            1 -> "the motor ${motors.joinToString { it.name }}"
//            else -> "the motors ${motors.joinToString { it.name }}"
//        }
//        telemetry.addData("[From MotorController of $motorsControlling]", data)
//    }
//
//    protected fun <T> warn(warning: T?) {
//        output("Warning: $warning")
//    }
//
//    fun addMotor(name: String, power: Float = 1f) {
//        addMotor(DcMotorInfo.fromName(name, power))
//    }
//
//    fun addMotor(motor: DcMotor, power: Float = 1f) {
//        addMotor(DcMotorInfo.fromMotor(motor, power))
//    }
//
//    fun addMotor(motor: DcMotorInfo) {
//        motors.add(motor)
//    }
//
//    protected inline fun <T> setAll(value: T, with: (motor: DcMotorInfo, value: T) -> Unit) {
//        motors.forEach { motor ->
//            with(motor, value)
//        }
//    }
//
//    abstract fun stop()
//
//    open fun go() {
//        currentPower = defaultPower
//    }
//}
//
//abstract class NonPositionalMotorController @JvmOverloads constructor(
//    opMode: OpMode,
//    motors: HashSet<DcMotorInfo>,
//    powerMultiplier: Float = 1f,
//    brake: Boolean/*?*/ = true): MotorController(opMode, motors, powerMultiplier, brake) {
//
//    var currentDirection: Direction = Direction.STOP
//
//    /**
//     * Stores the current unsigned power of all of the motors
//     * together. Although this may not be the actual power
//     * of any given individual motor, their unsigned powers
//     * will all be equal to this value multiplied by their
//     * individual power multipliers as specified in their
//     * DcMotorInfo objects.
//     *
//     * Note that, as an unsigned value, this property only stores
//     * the magnitude of the power, and not the direction. Upon
//     * setting this property with a non-positive value, the
//     * {@link #currentDirection()} will be updated appropriately
//     * and the currentPower will be set to the absolute value of
//     * the value.
//     */
//    override var currentPower = 0f
//        set(value) {
//            field = abs(value)
//            currentDirection = when {
//                value > 0f -> Direction.FORWARDS
//                value < 0f -> Direction.BACKWARDS
//                else       -> Direction.STOP // if zero
//            }
//            setAll(value = value, with = ::moveMotor)
//        }
//
//    abstract fun moveMotor(motor: DcMotorInfo, power: Float)
//
//    fun move(power: Float) {
//        currentPower = power
//    }
//
//    fun move(power: Float, direction: Direction) {
//        currentDirection = direction
//        currentPower = abs(power)
//        move(abs(power) * direction.multiplier)
//    }
//
//    override fun stop() {
//        currentPower = 0f
//    }
//}
//
//class PowerBasedNonPositionalMotorController @JvmOverloads constructor(
//    opMode: OpMode,
//    motors: HashSet<DcMotorInfo>,
//    powerMultiplier: Float = 1f,
//    brake: Boolean = true): NonPositionalMotorController(opMode, motors, powerMultiplier, brake) {
//
//    override fun moveMotor(motor: DcMotorInfo, power: Float) {
//        motor.encoderMode = DcMotor.RunMode.RUN_USING_ENCODER
//        motor.runPower = power
//    }
//}
//
//class SpeedBasedNonPositionalMotorController @JvmOverloads constructor(
//    opMode: OpMode,
//    motors: HashSet<DcMotorInfo>,
//    powerMultiplier: Float = 1f,
//    brake: Boolean = true): NonPositionalMotorController(opMode, motors, powerMultiplier, brake) {
//
//    override fun moveMotor(motor: DcMotorInfo, power: Float) {
//        motor.encoderMode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//        motor.runPower = power
//    }
//
//
//}
//
//class PositionalMotorController @JvmOverloads constructor(
//    opMode: OpMode,
//    motors: HashSet<DcMotorInfo>,
//    powerMultiplier: Float = 1f,
//    brake: Boolean = true,
//    encoderMode: DcMotor.RunMode = DcMotor.RunMode.RUN_USING_ENCODER): MotorController(opMode, motors, powerMultiplier, brake) {
//
//    var targetPosition = 0
//        set(value) {
//            field = value
//            setAll(value) { motor, targetPosition ->
//                motor.encoderMode = DcMotor.RunMode.RUN_TO_POSITION
//                motor.targetPosition = targetPosition
//            }
//        }
//
//    override var currentPower = 0f
//        set(value) {
//            field = value
//            setAll(value) { motor, power ->
//                motor.encoderMode = DcMotor.RunMode.RUN_TO_POSITION
//                motor.runPower = power
//            }
//        }
//
//    override fun stop() {
//        currentPower = 0f
//    }
//}