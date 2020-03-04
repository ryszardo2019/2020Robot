/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;





import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Timer;

//Things that I added. They might be useful
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.networktables.*;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  private boolean m_LimelightHasValidTarget = false;
	private double m_LimelightDriveCommand = 0.0;
	private double m_LimelightSteerCommand = 0.0;
  
  // Drivetrain
  SpeedControllerGroup left;
  SpeedControllerGroup right;

  Joystick joy_driver;
  Joystick joy_help;

  // Intake
  SpeedControllerGroup intake;
  SpeedControllerGroup index;

  // Shooter
  SpeedControllerGroup shooter;

  Timer autoTimer;

  Solenoid drive_right;
  Solenoid drive_left;

  //DoubleSolenoid climb_right = new DoubleSolenoid(2,0,1);
  //DoubleSolenoid climb_left = new DoubleSolenoid(2,0,1/*fill in*/);
  //DoubleSolenoid intakes = new DoubleSolenoid(2,0,1/*fill in*/);

  Solenoid right_drive = new Solenoid(0);
  Solenoid left_drive = new Solenoid(1);

  NetworkTableEntry tx;
	NetworkTableEntry ty;
	NetworkTableEntry ta;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    /*
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);*/

    CANSparkMax left1 = new CANSparkMax(1, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax left2 = new CANSparkMax(2, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax left3 = new CANSparkMax(3, CANSparkMaxLowLevel.MotorType.kBrushless);

    CANSparkMax right1 = new CANSparkMax(5, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax right2 = new CANSparkMax(4, CANSparkMaxLowLevel.MotorType.kBrushless);
    CANSparkMax right3 = new CANSparkMax(7, CANSparkMaxLowLevel.MotorType.kBrushless);

    left = new SpeedControllerGroup(left1, left2, left3);
    right = new SpeedControllerGroup(right1, right2, right3);
    right.setInverted(true);
    
    //The channel part of the intake
    Spark intakePWM1 = new Spark(0);
    //The folding out part of the intke
    Spark intakePWM2 = new Spark(1);
    intakePWM2.setInverted(true);
    intake = new SpeedControllerGroup(intakePWM2);
    index = new SpeedControllerGroup(intakePWM1);
    
    //controlls to the shooting mechanism
    Spark shooter1 = new Spark(2);
    //Spark shooter2 = new Spark(3);
    shooter = new SpeedControllerGroup(shooter1);
    index = new SpeedControllerGroup(intakePWM1);

    //The two x-box controllers
    joy_driver = new Joystick(0);
    joy_help = new Joystick(1);

    //Limelight fun
    NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
		tx = table.getEntry("tx");
		ty = table.getEntry("ty");
		ta = table.getEntry("ta");
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    autoTimer.reset();
    autoTimer.start();
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    /*
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
    */

    if(autoTimer.get() < 1){
      left.set(.5);
      right.set(0);
    }
    else if(autoTimer.get() < 5){
      left.set(1);
      right.set(1);
    }
    else if(autoTimer.get() < 6){
      right.set(.5);
      left.set(0);
    }
    else if(autoTimer.get() < 10){
      shooter.set(1);
      intake.set(1);
      right.set(0);
      left.set(0);
    }
    else if(autoTimer.get() < 11){
      right.set(.2);
      left.set(0);
    }
    else if(autoTimer.get() < 15){
      right.set(1);
      left.set(1);
      shooter.set(1);
      intake.set(1);
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    double leftSpeed = -joy_driver.getRawAxis(1);
    double leftMod = leftSpeed*leftSpeed*leftSpeed;
    double rightSpeed = -joy_driver.getRawAxis(5);
    double rightMod = rightSpeed*rightSpeed*rightSpeed;

    if(joy_driver.getRawButton(3) && m_LimelightHasValidTarget){
      drive(m_LimelightDriveCommand, m_LimelightSteerCommand);
    }
    else{
      drive(leftMod, rightMod);
    }

    double intakeSpeed = joy_driver.getRawAxis(2);
    intake.set(-intakeSpeed);
    /*Maybe?
    if(joy_driver.getRawButton(6)){
      intake.set(1);
    }*/
    if (joy_help.getRawButton(6)){
      index.set(.25);
    }
    /*
    double shooterSpeed = joy_driver.getRawAxis(3);
    shooter.set(-shooterSpeed);*/
    double indexSpeed = -1;
    double shooterSpeed = -.77;
    if(joy_driver.getRawButton(4)){
      shooter.set(shooterSpeed);
      index.set(indexSpeed);
    }

    //For shifting the transmition on the robot.
    if(joy_help.getRawButton(4)){
      right_drive.set(true);
      left_drive.set(true);
    }
    else{
      right_drive.set(false);
      left_drive.set(false);
    }

    /*
    //For moving the intake up and down.
    if(joy_help.getRawButton(6)){
      intakes.set(DoubleSolenoid.Value.kForward);
    }
    else if(joy_help.getRawAxis(3) > .75){
      intakes.set(DoubleSolenoid.Value.kReverse);
    }
    else{
      intakes.set(DoubleSolenoid.Value.kOff);
    }


    //This is for the climber
    if(joy_help.getRawButton(5)){
      climb_right.set(DoubleSolenoid.Value.kForward);
      climb_left.set(DoubleSolenoid.Value.kForward);
    }
    else if(joy_help.getRawAxis(2) > .75){
      climb_right.set(DoubleSolenoid.Value.kReverse);
      climb_left.set(DoubleSolenoid.Value.kReverse);
    }
    else{
      climb_right.set(DoubleSolenoid.Value.kOff);
      climb_left.set(DoubleSolenoid.Value.kOff);
    }*/
  }
  public void Update_Limelight_Tracking()
  {
  	// These numbers must be tuned for your Robot!  Be careful!
  	final double STEER_K = 0.02;                    // how hard to turn toward the target
  	final double DRIVE_K = 0.05;                    // how hard to drive fwd toward the target
  	final double DESIRED_TARGET_AREA = 13.0;        // Area of the target when the robot reaches the wall
  	final double MAX_DRIVE = 0.10;                   // Simple speed limit so we don't drive too fast

  	double tv = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);
  	double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
  	double ty = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ty").getDouble(0);
  	double ta = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ta").getDouble(0);

  	if (tv < 1.0)
  	{
  	  m_LimelightHasValidTarget = false;
  	  m_LimelightDriveCommand = 0.0;
  	  m_LimelightSteerCommand = 0.0;
  	  return;
  	}

  	m_LimelightHasValidTarget = true;

  	// Start with proportional steering
  	double steer_cmd = tx * STEER_K;
  	m_LimelightSteerCommand = steer_cmd;

  	// try to drive forward until the target area reaches our desired area
  	double drive_cmd = (DESIRED_TARGET_AREA - ta) * DRIVE_K;

  	// don't let the robot drive too fast into the goal
  	if (drive_cmd > MAX_DRIVE)
  	{
  	  drive_cmd = MAX_DRIVE;
  	}
    m_LimelightDriveCommand = drive_cmd;
  }
  public void drive(double l, double r){
		//double speedModifier = .8;
		left.set(l/**speedModifier*/);
		right.set(r/**speedModifier*/);
	}

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
