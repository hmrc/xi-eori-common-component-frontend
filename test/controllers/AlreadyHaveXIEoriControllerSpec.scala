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
import play.api.test.Helpers._
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  EstablishmentAddress,
  SubscriptionDisplayResponseDetail,
  XiSubscription
}
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import java.time.LocalDate
import scala.concurrent.Future

class AlreadyHaveXIEoriControllerSpec extends BaseSpec {

  val establishmentAddress: EstablishmentAddress = EstablishmentAddress(
    streetAndNumber = "line1",
    city = "City name",
    postalCode = Some("SE28 1AA"),
    countryCode = "GB"
  )

  val xiSubscription: XiSubscription = XiSubscription("XI8989989797", None)

  val subscriptionDisplayResponse: SubscriptionDisplayResponseDetail = SubscriptionDisplayResponseDetail(
    EORINo = Some("GB123456789012"),
    CDSFullName = "FirstName LastName",
    CDSEstablishmentAddress = establishmentAddress,
    VATIDs = None,
    shortName = Some("Short Name"),
    dateOfEstablishment = Some(LocalDate.now()),
    XI_Subscription = Some(xiSubscription)
  )

  "AlreadyHaveXIEori controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(subscriptionDisplayResponse))
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.AlreadyHaveXIEoriController.xiEoriAlreadyExists().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("You already have an XI EORI connected to this Government Gateway")
      }
    }

    "redirect to sign out page when the user clicks Signout button" in {
      val signOutXpath = "//*[@id='sign-out']"
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(subscriptionDisplayResponse))
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.AlreadyHaveXIEoriController.xiEoriAlreadyExists().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.getElementsHref(signOutXpath) shouldBe "http://localhost:9553/bas-gateway/sign-out-without-state"
      }
    }

  }
}
