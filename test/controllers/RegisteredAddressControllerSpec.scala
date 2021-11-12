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
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.PBEAddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{AddressLookup, AddressLookupFailure, AddressLookupSuccess}
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.Future

class RegisteredAddressControllerSpec extends BaseSpec with BeforeAndAfterEach {
  val mockSessionCache                   = mock[SessionCache]
  private val addressLookupParams        = PBEAddressLookup("postcode", None)
  private val addressLookup              = AddressLookup("line1", "city", "postcode", "GB")
  private val mockAddressLookupConnector = mock[AddressLookupConnector]

  override protected def afterEach(): Unit = {
    reset(mockAddressLookupConnector)
    reset(mockSessionCache)
    super.afterEach()
  }

  override def application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[SessionCache].to(mockSessionCache),
    inject.bind[AddressLookupConnector].to(mockAddressLookupConnector)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  "RegisteredAddress controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.addressLookupParams(any())).thenReturn(Future.successful(Some(addressLookupParams)))
        when(mockAddressLookupConnector.lookup(any(), any())(any()))
          .thenReturn(Future.successful(AddressLookupSuccess(Seq(addressLookup))))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.RegisteredAddressController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("What is your registered company address?")
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.addressLookupParams(any())).thenReturn(Future.successful(Some(addressLookupParams)))
        when(mockAddressLookupConnector.lookup(any(), any())(any()))
          .thenReturn(Future.successful(AddressLookupSuccess(Seq(addressLookup))))
        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.RegisteredAddressController.submit().url,
          defaultUserId,
          Map("address" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Select you address from the list")
      }
    }

    "verify address lookup is invoked while submit" in {
      running(application) {
        when(mockSessionCache.addressLookupParams(any())).thenReturn(Future.successful(Some(addressLookupParams)))
        when(mockAddressLookupConnector.lookup(any(), any())(any()))
          .thenReturn(Future.successful(AddressLookupSuccess(Seq(AddressLookup("line1", "city", "postcode", "GB")))))

        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.RegisteredAddressController.submit().url,
          defaultUserId,
          Map("address" -> addressLookup.dropDownView)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER

        verify(mockAddressLookupConnector).lookup(any(), any())(any())
      }
    }

    "redirect to PBE Postcode Lookup if session cache doesn't hold postcode on page load" in {

      running(application) {

        withAuthorisedUser(defaultUserId, mockAuthConnector)

        when(mockSessionCache.addressLookupParams(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.RegisteredAddressController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER

        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEAddressLookupController.onPageLoad().url
      }
    }

    "redirect to PBE Postcode Lookup if session cache doesn't hold postcode on submit" in {

      running(application) {

        when(mockSessionCache.addressLookupParams(any())).thenReturn(Future.successful(None))
        when(mockAddressLookupConnector.lookup(any(), any())(any()))
          .thenReturn(Future.successful(AddressLookupSuccess(Seq(AddressLookup("line1", "city", "postcode", "GB")))))

        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.RegisteredAddressController.submit().url,
          defaultUserId,
          Map("address" -> addressLookup.dropDownView)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER

        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEAddressLookupController.onPageLoad().url
      }
    }

    "redirect when address lookup call fails" in {

      when(mockSessionCache.addressLookupParams(any())).thenReturn(Future.successful(Some(addressLookupParams)))
      when(mockAddressLookupConnector.lookup(any(), any())(any())).thenReturn(Future.successful(AddressLookupFailure))

      withAuthorisedUser(defaultUserId, mockAuthConnector)

      val request = SessionBuilder.buildRequestWithSessionAndPath(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.RegisteredAddressController.onPageLoad().url,
        defaultUserId
      )

      val result = route(application, request).get
      status(result) shouldBe SEE_OTHER

      redirectLocation(
        result
      ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.onPageLoad().url
    }

  }
}
