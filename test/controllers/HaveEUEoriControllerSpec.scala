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

package controllers

import common.pages.RegistrationPage
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.UserAnswersCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.GroupEnrolmentExtractor
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, Eori, KeyValue}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.HaveEUEori.{No, Yes}
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.Future

class HaveEUEoriControllerSpec extends BaseSpec {

  val groupEnrolment =
    List(EnrolmentResponse("HMRC-ATAR-ORG", "Activated", List(KeyValue("EORINumber", "GB123456463324"))))

  val existingEori: Option[Eori] = Some(Eori("XIE9XSDF10BCKEYAX"))

  val mockGroupEnrolmentExtractor: GroupEnrolmentExtractor = mock[GroupEnrolmentExtractor]

  "HaveEUEori controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getHaveEUEori()(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.onPageLoad().url
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith(
          "Do you have an EU Economic Operator Registration and Identification (EORI) number?"
        )
      }
    }
    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getHaveEUEori()(any())).thenReturn(Future.successful(Some(true)))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.onPageLoad().url
        )
        val result = route(application, request).get
        val page   = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='value']") shouldBe "yes"
      }
    }
    "redirect to the XIEoriNotNeeded page when user selects Yes" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.cacheHaveEUEori(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.submit().url,
          Map("value" -> Yes.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded().url
      }

    }

    "redirect to the Confirm details page when user has GBEori and selects No" in {
      def application = new GuiceApplicationBuilder().overrides(
        inject.bind[AuthConnector].to(mockAuthConnector),
        inject.bind[UserAnswersCache].to(mockUserAnswersCache),
        inject.bind[GroupEnrolmentExtractor].to(mockGroupEnrolmentExtractor)
      ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockGroupEnrolmentExtractor.groupIdEnrolments(any())(any()))
          .thenReturn(Future.successful(groupEnrolment))
        when(mockGroupEnrolmentExtractor.getEori(any())(any()))
          .thenReturn(Future.successful(existingEori))
        when(mockUserAnswersCache.cacheHaveEUEori(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.submit().url,
          Map("value" -> No.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.onPageLoad().url
      }

    }

    "return a Bad Request and errors when invalid data is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.submit().url,
          Map("value" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith(
          "Tell us if you have an EU Economic Operator Registration and Identification (EORI) number"
        )
      }
    }

  }
}
