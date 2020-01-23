//package org.firstinspires.ftc.teamcode;
//
//import com.qualcomm.robotcore.eventloop.opmode.Disabled;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.OpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotor;
//
//import org.firstinspires.ftc.robotcore.external.ClassFactory;
//import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
//import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
//import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
//import org.firstinspires.ftc.teamcode.helpers.Direction;
//
//import java.util.List;
//
//
//@TeleOp(name = "Vision Drive", group = "Iterative Opmode")
//
//public class Autonomous extends LinearOpMode {
//    private static final String TFOD_MODEL_ASSET = "Skystone.tflite";
//    private static final String LABEL_FIRST_ELEMENT = "Stone";
//    private static final String LABEL_SECOND_ELEMENT = "Skystone";
//
//    private static final String VUFORIA_KEY = "AZne3+j/////AAABmW73vsKOpUy1iIrLi6QIERVkROfCZRH7Psc0Hfu51ebH5+Rwr8HmBZkBBLk/KkETR6oBhjAWkR9Qwr0KySE3niFw7Xr5zG663LsKdTQB5Yhdv3gi+Oo75YIP2kdvZU4CdlvhaCThNDPNRb5/ca4qjm45ANH6HDyKqoHpdQLo6BLBT6md3ufUmuWSILQgxxWH0W4koG8KqpDnEN2nI1p6zFx1t9lSQkju4cxMqkkt5FSTHwlowPXZK0SzC/OTiUO0lbMewL9k2abO3+RdoaFPptvfVO8IXH7hP9BV+PuX1jgdjtd7cP301XnfdwE8U/x9TENp9pyjmr+jmlE4a3r5QTNM0k0iV61CPk/3VXlQzNDL";
//
//    private VuforiaLocalizer vuforia;
//
//    private TFObjectDetector tfod;
//
//
//    private float drivePower = 1;
//    private float K = 1;
//    private float armPower = 1;
//    float clawPower;
//    float slidePower;
//
//    //initialize directions
//    private Direction armDirection = Direction.STOP;
//    Direction clawDirection = Direction.STOP;
//    Direction slideDirection = Direction.STOP;
//
//    private boolean isPressed(float button) {
//        return button > 0.1; //change this value if necessary
//    }
//
////    private MotorController frontLeft = null;
////    private MotorController frontRight = null;
////    private MotorController backLeft = null;
////    private MotorController backRight = null;
////    private MotorController arm = null;
////    private MotorController intake = null;
////    private MotorController liftRight = null;
////    private MotorController liftLeft = null;
//
//    private DcMotor frontLeft = null;
//    private DcMotor frontRight = null;
//    private DcMotor backLeft = null;
//    private DcMotor backRight = null;
//    private DcMotor arm = null;
//    private DcMotor intake = null;
//    private DcMotor liftRight = null;
//    private DcMotor liftLeft = null;
//
//    @Override
//    public void runOpMode() {
//        telemetry.addData("Status", "Initialized");
//
//        // Initialize the hardware variables. Note that the strings used here as parameters
//        // to 'get' must correspond to the names assigned during the robot configuration
//        // step (using the FTC Robot Controller app on the phone).
//        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
//        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
//        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
//        backRight = hardwareMap.get(DcMotor.class, "backRight");
//        arm = hardwareMap.get(DcMotor.class, "arm");
//        intake = hardwareMap.get(DcMotor.class, "intake");
//        liftRight = hardwareMap.get(DcMotor.class, "liftRight");
//        liftLeft = hardwareMap.get(DcMotor.class, "liftLeft");
//        frontLeft.setDirection(DcMotor.Direction.REVERSE);
//        backLeft.setDirection(DcMotor.Direction.REVERSE);
//
//        liftLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//        liftRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
//
//        //Display Initialized
//        telemetry.addData("Status", "Initialized");
//
//
//
//        // The TFObjectDetector uses the camera frames from the VuforiaLocalizer, so we create that
//        // first.
//        initVuforia();
//
//        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
//            initTfod();
//        } else {
//            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
//        }
//
//        /**
//         * Activate TensorFlow Object Detection before we wait for the start command.
//         * Do it here so that the Camera Stream window will have the TensorFlow annotations visible.
//         **/
//        if (tfod != null) {
//            tfod.activate();
//        }
//
//        /** Wait for the game to begin */
//        telemetry.addData(">", "Press Play to start op mode");
//        telemetry.update();
//        waitForStart();
//
//        if (opModeIsActive()) {
//            while (opModeIsActive()) {
//                if (tfod != null) {
//                    // getUpdatedRecognitions() will return null if no new information is available since
//                    // the last time that call was made.
//                    List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
//                    if (updatedRecognitions != null) {
//                        telemetry.addData("# Object Detected", updatedRecognitions.size());
//
//                        // step through the list of recognitions and display boundary info.
//                        int i = 0;
//                        for (Recognition recognition : updatedRecognitions) {
//                            telemetry.addData(String.format("label (%d)", i), recognition.getLabel());
//                            telemetry.addData(String.format("  left,top (%d)", i), "%.03f , %.03f",
//                                    recognition.getLeft(), recognition.getTop());
//                            telemetry.addData(String.format("  right,bottom (%d)", i), "%.03f , %.03f",
//                                    recognition.getRight(), recognition.getBottom());
//                        }
//                        telemetry.update();
//                    }
//                }
//            }
//        }
//
//        if (tfod != null) {
//            tfod.shutdown();
//        }
//    }
//
//    /**
//     * Initialize the Vuforia localization engine.
//     */
//    private void initVuforia() {
//        /*
//         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
//         */
//        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
//
//        parameters.vuforiaLicenseKey = VUFORIA_KEY;
//        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
//
//        //  Instantiate the Vuforia engine
//        vuforia = ClassFactory.getInstance().createVuforia(parameters);
//
//        // Loading trackables is not necessary for the TensorFlow Object Detection engine.
//    }
//
//    /**
//     * Initialize the TensorFlow Object Detection engine.
//     */
//    private void initTfod() {
//        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
//                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
//        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
//        tfodParameters.minimumConfidence = 0.8;
//        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
//        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_FIRST_ELEMENT, LABEL_SECOND_ELEMENT);
//    }
//}
//
