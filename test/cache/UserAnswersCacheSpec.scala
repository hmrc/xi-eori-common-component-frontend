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

package cache

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, ArgumentMatchers}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.await
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.Eori
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.RegistrationDetails
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.{
  ConfirmDetails,
  DisclosePersonalDetails,
  HaveEUEori,
  HavePBE,
  TradeWithNI
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel
import util.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, Future}
import scala.util.Random

class UserAnswersCacheSpec extends BaseSpec with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  private val mockRegistrationDetails           = mock[RegistrationDetails]
  private val mockpersonalDataDisclosureConsent = mock[Option[Boolean]]

  private val addressDetails =
    AddressViewModel(street = "street", city = "city", postcode = Some("postcode"), countryCode = "GB")

  private val subscriptionDetailsHolderService =
    new UserAnswersCache(mockSessionCache)

  private val eoriNumericLength = 15
  private val eoriId            = "GB" + Random.nextString(eoriNumericLength)
  private val eori              = Eori(eoriId)

  protected override def beforeEach {
    reset(mockSessionCache, mockRegistrationDetails)

    when(mockSessionCache.saveRegistrationDetails(any[RegistrationDetails])(any[HeaderCarrier]))
      .thenReturn(Future.successful(true))

    val existingHolder = RegistrationDetails(None, None, None, None, None, None, None)

    when(mockSessionCache.registrationDetails(any[HeaderCarrier])).thenReturn(Future.successful(existingHolder))
    when(mockRegistrationDetails.personalDataDisclosureConsent).thenReturn(mockpersonalDataDisclosureConsent)

  }

  "Calling cacheAddressDetails" should {
    "save Address Details in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheAddressDetails(addressDetails), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[RegistrationDetails])

      verify(mockSessionCache).saveRegistrationDetails(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: RegistrationDetails = requestCaptor.getValue
      holder.addressDetails shouldBe Some(addressDetails)

    }

    "should not save emptry strings in postcode field" in {

      Await.result(
        subscriptionDetailsHolderService.cacheAddressDetails(addressDetails.copy(postcode = Some(""))),
        Duration.Inf
      )
      val requestCaptor = ArgumentCaptor.forClass(classOf[RegistrationDetails])

      verify(mockSessionCache).saveRegistrationDetails(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: RegistrationDetails = requestCaptor.getValue
      holder.addressDetails shouldBe Some(addressDetails.copy(postcode = None))
    }
  }

  "Calling cachePersonalDataDisclosureConsent" should {
    "save Personal Data Disclosure Consent in frontend cache" in {

      Await.result(
        subscriptionDetailsHolderService.cacheConsentToDisclosePersonalDetails(DisclosePersonalDetails.Yes),
        Duration.Inf
      )
      val requestCaptor = ArgumentCaptor.forClass(classOf[RegistrationDetails])

      verify(mockSessionCache).saveRegistrationDetails(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: RegistrationDetails = requestCaptor.getValue
      holder.personalDataDisclosureConsent shouldBe Some(true)
    }
  }
  "Calling cacheTradeWithNI" should {
    "save TradeWithNI in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheTradeWithNI(TradeWithNI.Yes), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[RegistrationDetails])

      verify(mockSessionCache).saveRegistrationDetails(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: RegistrationDetails = requestCaptor.getValue
      holder.tradeWithNI shouldBe Some(true)
    }
  }
  "Calling cacheHaveEUEori" should {
    "save HaveEUEori in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheHaveEUEori(HaveEUEori.Yes), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[RegistrationDetails])

      verify(mockSessionCache).saveRegistrationDetails(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: RegistrationDetails = requestCaptor.getValue
      holder.haveEUEori shouldBe Some(true)
    }
  }
  "Calling cacheHavePBEInNI" should {
    "save HavePBEInNI in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheHavePBEInNI(HavePBE.Yes), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[RegistrationDetails])

      verify(mockSessionCache).saveRegistrationDetails(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: RegistrationDetails = requestCaptor.getValue
      holder.havePBEInNI shouldBe Some(true)
    }
  }
  "Calling cacheConfirmDetails" should {
    "save ConfirmDetails in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheConfirmDetails(ConfirmDetails.changeDetails), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[RegistrationDetails])

      verify(mockSessionCache).saveRegistrationDetails(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: RegistrationDetails = requestCaptor.getValue
      holder.confirmDetails shouldBe Some("changeDetails")
    }
  }
  "Calling cacheSicCode" should {
    "save SicCode in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheSicCode("99978"), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[RegistrationDetails])

      verify(mockSessionCache).saveRegistrationDetails(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: RegistrationDetails = requestCaptor.getValue
      holder.sicCode shouldBe Some("99978")

    }
  }

}
