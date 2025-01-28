import com.uor.eng.service.DatabaseService;
import com.uor.eng.ui.LoginWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
  @Override
  public void start(Stage primaryStage) {
    DatabaseService databaseService = new DatabaseService();
    LoginWindow loginWindow = new LoginWindow(databaseService, primaryStage);
    loginWindow.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
