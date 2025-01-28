import com.uor.eng.model.User;
import com.uor.eng.service.DatabaseService;
import com.uor.eng.service.MonitoringService;
import com.uor.eng.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    DatabaseService databaseService = new DatabaseService();
    MonitoringService monitoringService = new MonitoringService(databaseService);

    // Simulate login for testing
    User currentUser = new User(1L, "Test User", "test@example.com", "testuser", "password");

    MainWindow mainWindow = new MainWindow(databaseService, monitoringService, currentUser);
    mainWindow.show(primaryStage);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
