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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{inject, Application}
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.UserAnswersCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.GroupEnrolmentExtractor
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, KeyValue}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.Future

class ManualContactAddressControllerSpec extends BaseSpec {

  val mockGroupEnrolmentExtractor: GroupEnrolmentExtractor = mock[GroupEnrolmentExtractor]

  private def groupEnrolment() =
    List(EnrolmentResponse("HMRC-ATAR-ORG", "Activated", List(KeyValue("EORINumber", "GB123456463324"))))

  override def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[UserAnswersCache].to(mockUserAnswersCache),
    inject.bind[GroupEnrolmentExtractor].to(mockGroupEnrolmentExtractor)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  "ManualContactAddress controller" should {
    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getContactAddressDetails()(any())).thenReturn(
          Future.successful(Some(AddressViewModel("line1", "town", Some("BT11AA"), "GB", Some(""), Some(""))))
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualContactAddressController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='line1']") shouldBe "line1"
      }
    }

    "return OK and the correct view for review" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualContactAddressController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("What is your XI EORI application contact address?")
      }
    }

    "return a Bad Request and errors when no line1 is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualContactAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "", "townorcity" -> "test", "postcode" -> "BT11AA")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Enter Address line 1")

      }
    }
  }
}
