package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.robotcore.external.tfod.Recognition
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector
import org.firstinspires.ftc.teamcode.helpers.Direction
import java.lang.Math.*
import java.util.*
import java.util.concurrent.TimeUnit


@Autonomous(name = "Vision Drive", group = "Iterative Opmode")
class Autonomous : LinearOpMode() {

    private var vuforia: VuforiaLocalizer? = null

    private var tfod: TFObjectDetector? = null


    private val drivePower = 1f
    private val K = 1f
    private val armPower = 1f
    internal var clawPower: Float = 0f
    internal var slidePower: Float = 0f

    //initialize directions
    private var armDirection = Direction.STOP
    internal var clawDirection = Direction.STOP
    internal var slideDirection = Direction.STOP

    //    private MotorController frontLeft = null;
    //    private MotorController frontRight = null;
    //    private MotorController backLeft = null;
    //    private MotorController backRight = null;
    //    private MotorController arm = null;
    //    private MotorController intake = null;
    //    private MotorController liftRight = null;
    //    private MotorController liftLeft = null;

    private var frontLeft: DcMotor? = null
    private var frontRight: DcMotor? = null
    private var backLeft: DcMotor? = null
    private var backRight: DcMotor? = null
    private var arm: DcMotor? = null
    private var intake: DcMotor? = null
    private var liftRight: DcMotor? = null
    private var liftLeft: DcMotor? = null

    private fun isPressed(button: Float): Boolean {
        return button > 0.1 //change this value if necessary
    }

    override fun runOpMode() {
        telemetry.addData("Status", "Initialized")

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        frontLeft = hardwareMap.get(DcMotor::class.java, "frontLeft")
        frontRight = hardwareMap.get(DcMotor::class.java, "frontRight")
        backLeft = hardwareMap.get(DcMotor::class.java, "backLeft")
        backRight = hardwareMap.get(DcMotor::class.java, "backRight")
        arm = hardwareMap.get(DcMotor::class.java, "arm")
        intake = hardwareMap.get(DcMotor::class.java, "intake")
        liftRight = hardwareMap.get(DcMotor::class.java, "liftRight")
        liftLeft = hardwareMap.get(DcMotor::class.java, "liftLeft")


        frontLeft!!.direction = DcMotorSimple.Direction.REVERSE
        backLeft!!.direction = DcMotorSimple.Direction.REVERSE

        liftLeft!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftRight!!.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        //Display Initialized
        telemetry.addData("Status", "Initialized")


        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
        // first.
        initVuforia()

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod()
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD")
        }

        /**
         * Activate TensorFlow Object Detection before we wait for the start command.
         * Do it here so that the Camera Stream window will have the TensorFlow annotations visible.
         */

        tfod?.activate()

        /** Wait for the game to begin  */
        telemetry.addData(">", "Press Play to start op mode")
        telemetry.update()
        waitForStart()

        if (opModeIsActive()) {
//            while (opModeIsActive()) {
//                if (tfod != null) {
//                    // getUpdatedRecognitions() will return null if no new information is available since
//                    // the last time that call was made.
//                    val updatedRecognitions = tfod!!.updatedRecognitions
//                    if (updatedRecognitions != null) {
//                        telemetry.addData("# Object Detected", updatedRecognitions.size)
//
//                        // step through the list of recognitions and display boundary info.
//                        val i = 0
//                        for (recognition in updatedRecognitions) {
//                            telemetry.addData(String.format("label (%d)", i), recognition.label)
//                            telemetry.addData(
//                                String.format("  left,top (%d)", i), "%.03f , %.03f",
//                                recognition.left, recognition.top
//                            )
//                            telemetry.addData(
//                                String.format("  right,bottom (%d)", i), "%.03f , %.03f",
//                                recognition.right, recognition.bottom
//                            )
//                        }
//                        telemetry.update()
//                    }
//                }
//            }
        }

        if (tfod != null) {
            tfod!!.shutdown()
        }

        armDirection = Direction.FORWARDS
        grab()
        TimeUnit.SECONDS.sleep(1)
        armDirection = Direction.STOP
        grab()

        driveSeconds(4, 0.5)

        slideDirection = Direction.FORWARDS
        grab()
        TimeUnit.MILLISECONDS.sleep(100)
        slideDirection = Direction.STOP
        grab()
//        telemetry.addData("info", "gonna drive")
//        drive(0.5, 0.0, 0.0)
//        telemetry.addData("info", "drove")
//        telemetry.update()
//        TimeUnit.SECONDS.sleep(1)
//        telemetry.addData("info", "waited")
//        telemetry.update()
//        stopDriving()
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private fun initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        val parameters = VuforiaLocalizer.Parameters()

        parameters.vuforiaLicenseKey = VUFORIA_KEY
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters)

        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
    }

    /**
     * Initialize the TensorFlow Object Detection engine.
     */
    private fun initTfod() {
        val tfodMonitorViewId = hardwareMap.appContext.resources.getIdentifier(
            "tfodMonitorViewId", "id", hardwareMap.appContext.packageName
        )
        val tfodParameters = TFObjectDetector.Parameters(tfodMonitorViewId)
        tfodParameters.minimumConfidence = 0.8
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia)
        tfod!!.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT)
    }

    companion object {
        private val TFOD_MODEL_ASSET = "Skystone.tflite"
        private val LABEL_FIRST_ELEMENT = "Stone"
        private val LABEL_SECOND_ELEMENT = "Skystone"

        private val VUFORIA_KEY =
            "AZne3+j/////AAABmW73vsKOpUy1iIrLi6QIERVkROfCZRH7Psc0Hfu51ebH5+Rwr8HmBZkBBLk/KkETR6oBhjAWkR9Qwr0KySE3niFw7Xr5zG663LsKdTQB5Yhdv3gi+Oo75YIP2kdvZU4CdlvhaCThNDPNRb5/ca4qjm45ANH6HDyKqoHpdQLo6BLBT6md3ufUmuWSILQgxxWH0W4koG8KqpDnEN2nI1p6zFx1t9lSQkju4cxMqkkt5FSTHwlowPXZK0SzC/OTiUO0lbMewL9k2abO3+RdoaFPptvfVO8IXH7hP9BV+PuX1jgdjtd7cP301XnfdwE8U/x9TENp9pyjmr+jmlE4a3r5QTNM0k0iV61CPk/3VXlQzNDL"
    }

    private fun drive(forward: Double, right: Double = 0.0, clockwise: Double = 0.0) {

//        val forward = (-gamepad1.left_stick_y).toDouble() // push joystick1 forward to go forward
//        val right =
//            (-gamepad1.left_stick_x).toDouble() // push joystick1 to the right to strafe right
//        val clockwise =
//            (-gamepad1.right_stick_x).toDouble() // push joystick2 to the right to rotate clockwise

        var front_left = forward + right + K * clockwise
        var front_right = forward - right - K * clockwise
        var rear_left = forward - right + K * clockwise
        var rear_right = forward + right - K * clockwise


        val max = Collections.max(
            listOf(
                abs(front_left),
                abs(front_right),
                abs(rear_left),
                abs(rear_right)//,
            )
        ) // gets the largest absolute value of the direction powers

        if (max > 1) {
            front_left /= max
            front_right /= max
            rear_left /= max
            rear_right /= max
        }

        frontLeft!!.setPower(front_left * drivePower)
        frontRight!!.setPower(front_right * drivePower)
        backRight!!.setPower(rear_right * drivePower)
        backLeft!!.setPower(rear_left * drivePower)

    }

    private fun stopDriving() = drive(0.0)

    private fun driveSeconds(seconds: Long, forward: Double = 0.0, right: Double = 0.0, clockwise: Double = 0.0) {
        drive(forward, right, clockwise)
        TimeUnit.SECONDS.sleep(seconds)
        stopDriving()
    }

    private fun grab() {
        //        switch (armDirection) {
        //            case FORWARDS:
        //                arm.setTargetPosition(0);
        //                arm.setPower(armPower);
        //                break;
        //            case BACKWARDS:
        //                arm.setTargetPosition(-2000);
        //                arm.setPower(armPower);
        //                break;
        //            case STOP:
        //                arm.setPower(0);
        //                break;
        //        }

        arm!!.power = (armDirection.multiplier * armPower).toDouble()
        intake!!.power = (clawDirection.multiplier * clawPower).toDouble()
        moveSlide((slideDirection.multiplier * slidePower).toDouble())
    }

    private fun moveSlide(power: Double) {
        liftLeft!!.setPower(power)
        liftRight!!.setPower(-power)
    }
}

