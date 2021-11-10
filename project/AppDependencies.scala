import play.core.PlayVersion
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28" % "5.14.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "1.14.0-play-28",
    "uk.gov.hmrc"             %% "mongo-caching"              % "7.0.0-play-28",
  )

  val test = Seq(
    "org.scalatest"           %% "scalatest"                  % "3.0.8"             % "test,it",
    "com.typesafe.play"       %% "play-test"                  % PlayVersion.current % "test,it",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.0.0"             % "test,it",
    "com.github.tomakehurst"  % "wiremock-standalone"         % "2.23.2"            % "test, it"
      exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "org.scalacheck"          %% "scalacheck"                 % "1.14.0"            % "test,it",
    "org.jsoup"               % "jsoup"                       % "1.11.3"            % "test,it",
    "us.codecraft"            % "xsoup"                       % "0.3.1"             % "test,it",
    "org.mockito"             % "mockito-core"                % "3.0.0"             % "test,it",
    "org.pegdown"             % "pegdown"                     % "1.6.0",
    "uk.gov.hmrc"             %% "reactivemongo-test"         % "5.0.0-play-28"     % "test, it"
  )
}
