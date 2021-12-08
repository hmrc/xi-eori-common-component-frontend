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
import play.api.{inject, Application}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.GroupEnrolmentExtractor
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, KeyValue}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.PBEAddressLookup
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.Future

class AddressLookupErrorControllerSpec extends BaseSpec {
  val reenterXpath                                         = "//*[@id='reenter-postcode-button']"
  val mockGroupEnrolmentExtractor: GroupEnrolmentExtractor = mock[GroupEnrolmentExtractor]
  private val addressLookupParams                          = PBEAddressLookup("BT281AF", None)

  val groupEnrolment =
    List(EnrolmentResponse("HMRC-ATAR-ORG", "Activated", List(KeyValue("EORINumber", "GB123456463324"))))

  override def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[SessionCache].to(mockSessionCache),
    inject.bind[GroupEnrolmentExtractor].to(mockGroupEnrolmentExtractor)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  "AddressLookupError controller" should {

    "display Error page" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.AddressLookupErrorController.displayErrorPage().url
        )
        val result = route(application, request).get
        status(result) shouldBe OK

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("We have a problem")

      }
    }

    "display No Address Found page" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockGroupEnrolmentExtractor.groupIdEnrolments(any())(any()))
          .thenReturn(Future.successful(groupEnrolment))
        when(mockSessionCache.addressLookupParams(any())).thenReturn(Future.successful(Some(addressLookupParams)))

        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.AddressLookupErrorController.displayNoResultsPage().url
        )
        val result = route(application, request).get
        status(result) shouldBe OK

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("No address found")

      }
    }
  }
}
