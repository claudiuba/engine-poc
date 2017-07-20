package com.blueprint.engine.transform.config

import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
  * Created by raduchilom on 18/07/2017.
  */
object Config {

  val properties = ConfigFactory.load()

  val masterUrl = properties.getString("masterUrl")
  val appName = properties.getString("appName")
  val sparkSqlPart = properties.getInt("spark.sql.shuffle.partitions")

  private def initSparkSession = {
    val conf = new SparkConf().setMaster(Config.masterUrl)
    conf.set("spark.sql.shuffle.partitions", sparkSqlPart.toString)
    SparkSession
      .builder()
      .appName(Config.appName)
      .config(conf)
      .getOrCreate()
  }

  val sparkSession = initSparkSession

}
