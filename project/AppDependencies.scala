import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-frontend-play-28" % "5.14.0",
    "uk.gov.hmrc"             %% "play-frontend-hmrc"         % "1.14.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % "5.14.0"            % Test,
    "org.jsoup"               %  "jsoup"                      % "1.13.1"            % Test,
    "us.codecraft"            %  "xsoup"                      % "0.3.1"             % "test,it",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.36.8"            % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0"             % Test,
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.7.1"             % Test,
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.0.0"             % "test,it",
    "com.github.tomakehurst"  % "wiremock-standalone"         % "2.23.2"            % "test, it"
      exclude("org.apache.httpcomponents", "httpclient") exclude("org.apache.httpcomponents", "httpcore"),
    "org.scalacheck"          %% "scalacheck"                 % "1.14.0"            % "test,it",
    "org.pegdown"             % "pegdown"                     % "1.6.0",
  )
}
