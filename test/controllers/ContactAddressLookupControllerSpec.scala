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
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ContactAddressLookup
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.Future

class ContactAddressLookupControllerSpec extends BaseSpec {

  override def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[SessionCache].to(mockSessionCache)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  "ContactAddressLookup controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.contactAddressParams(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressLookupController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("What is your XI EORI application contact address?")
      }
    }

    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.contactAddressParams(any())).thenReturn(
          Future.successful(Some(ContactAddressLookup("SE211AA", Some("line1"))))
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressLookupController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='postcode']") shouldBe "SE211AA"
        page.getElementValue("//*[@id='line1']") shouldBe "line1"
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressLookupController.submit().url,
          defaultUserId,
          Map("postcode" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Enter postcode")
      }
    }

    "redirect to Registered address when valid data is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.saveContactAddressParams(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressLookupController.submit().url,
          defaultUserId,
          Map("postcode" -> "BT28 1AA")
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER

        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressResultController.onPageLoad().url
      }
    }
  }
}
