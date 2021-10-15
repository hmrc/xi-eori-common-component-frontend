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

package controllers.auth

import play.api.mvc.{AnyContentAsEmpty, DefaultActionBuilder, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import play.test.Helpers.fakeRequest
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector, MissingBearerToken}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import util.ControllerSpec
import util.builders.AuthActionMock
import util.builders.AuthBuilder.{withAuthorisedUser, withNotLoggedInUser}
import util.builders.SessionBuilder.{addToken, sessionMap}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends ControllerSpec with AuthActionMock {

  class Harness(authAction: AuthAction) {

    def onPageLoad() = authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        Future.successful(Results.Ok)
    }

  }

  "AuthAction controller" should {

    "redirect to GG login page if the user is not logged in" in {
      running(application) {
        withNotLoggedInUser(mockAuthConnector)
        val configuration = instanceOf[Configuration]
        val environment   = Environment.simple()
        val actionBuilder = DefaultActionBuilder(stubBodyParser(AnyContentAsEmpty))
        val authAction = new AuthAction(
          configuration,
          environment,
          new FakeFailingAuthConnector(new MissingBearerToken),
          actionBuilder
        )
        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(FakeRequest())

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get should startWith("/bas-gateway/sign-in")

      }
    }

    "redirect to You cannot use service page if the Agent user logged in" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Agent)
        val configuration = instanceOf[Configuration]
        val environment   = Environment.simple()
        val actionBuilder = DefaultActionBuilder(stubBodyParser(AnyContentAsEmpty))
        val authAction    = new AuthAction(configuration, environment, mockAuthConnector, actionBuilder)
        val controller    = new Harness(authAction)
        val result        = controller.onPageLoad()(addToken(FakeRequest().withSession(sessionMap(defaultUserId): _*)))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get should startWith("/xi-customs-registration-services/you-cannot-use-service")

      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)

}
