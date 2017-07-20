import com.blueprint.engine.transform.config.Config

/**
  * Created by raduchilom on 18/07/2017.
  */
object Runner extends App {

  println("Running the App.")

  println(Config.sparkSession.emptyDataFrame.count())

}
