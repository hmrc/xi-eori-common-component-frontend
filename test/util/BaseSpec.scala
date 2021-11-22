/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util

import akka.stream.Materializer
import akka.stream.testkit.NoMaterializer
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.{DefaultFileMimeTypes, FileMimeTypesConfiguration}
import play.api.i18n.Lang._
import play.api.i18n.{Messages, MessagesApi, MessagesImpl}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{inject, Application, Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.config.AppConfig
import util.builders.{AuthBuilder, SessionBuilder}

import java.util.UUID
import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Random

trait BaseSpec extends WordSpec with MockitoSugar with Matchers with Injector {

  implicit val messagesApi: MessagesApi = instanceOf[MessagesApi]

  implicit def materializer: Materializer = NoMaterializer

  implicit val messages: Messages = MessagesImpl(defaultLang, messagesApi)

  implicit val mcc: MessagesControllerComponents = DefaultMessagesControllerComponents(
    new DefaultMessagesActionBuilderImpl(stubBodyParser(AnyContentAsEmpty), messagesApi)(global),
    DefaultActionBuilder(stubBodyParser(AnyContentAsEmpty))(global),
    stubPlayBodyParsers(NoMaterializer),
    messagesApi, // Need to be a real messages api, because our tests checks the content, not keys
    stubLangs(),
    new DefaultFileMimeTypes(FileMimeTypesConfiguration()),
    global
  )

  protected val previousPageUrl = "javascript:history.back()"

  val env: Environment                 = Environment.simple()
  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val config: Configuration            = Configuration.load(env)

  private val serviceConfig = new ServicesConfig(config)

  val appConfig: AppConfig = new AppConfig(config, serviceConfig)

  val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "")

  def postRequest(data: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest("POST", "").withFormUrlEncodedBody(data: _*)

  protected def assertNotLoggedInUserShouldBeRedirectedToLoginPage(
    mockAuthConnector: AuthConnector,
    actionDescription: String,
    action: Action[AnyContent]
  ): Unit =
    actionDescription should {
      "redirect to GG login when request is not authenticated" in {
        AuthBuilder.withNotLoggedInUser(mockAuthConnector)

        val result = action.apply(
          SessionBuilder.buildRequestWithSessionAndPathNoUser(method = "GET", path = s"/customs-registration-services/")
        )
        status(result) shouldBe SEE_OTHER
        header(LOCATION, result).get should include(
          "/bas-gateway/sign-in?continue_url=http%3A%2F%2Flocalhost%3A6751%2Fcustoms-registration-services%2Fatar%2Fregister&origin=eori-common-component-registration-frontend"
        )
      }
    }

  protected def assertNotLoggedInAndCdsEnrolmentChecksForGetAnEori(
    mockAuthConnector: AuthConnector,
    action: Action[AnyContent],
    additionalLabel: String = ""
  ): Unit =
    s"redirect to GG login when request is not authenticated when the Journey is for a Get An EORI Journey $additionalLabel" in {
      AuthBuilder.withNotLoggedInUser(mockAuthConnector)

      val result: Future[Result] = action.apply(
        SessionBuilder.buildRequestWithSessionAndPathNoUser(
          method = "GET",
          path = s"/customs-registration-services/atar/register/"
        )
      )
      status(result) shouldBe SEE_OTHER
      header(LOCATION, result).get should include(
        "/bas-gateway/sign-in?continue_url=http%3A%2F%2Flocalhost%3A6751%2Fcustoms-registration-services%2Fatar%2Fregister&origin=eori-common-component-registration-frontend"
      )
    }

  val defaultUserId: String = s"user-${UUID.randomUUID}"

  val mockSessionCache: SessionCache         = mock[SessionCache]
  val mockUserAnswersCache: UserAnswersCache = mock[UserAnswersCache]

  // TODO Extract below methods to some Utils class
  def strim(s: String): String = s.stripMargin.trim.split("\n").mkString(" ")

  def oversizedString(maxLength: Int): String = Random.alphanumeric.take(maxLength + 1).mkString

  def undersizedString(minLength: Int): String = Random.alphanumeric.take(minLength - 1).mkString

  def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[SessionCache].to(mockSessionCache),
    inject.bind[UserAnswersCache].to(mockUserAnswersCache)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

}
