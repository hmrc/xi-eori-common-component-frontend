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
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.UserAnswersCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.UserAnswers
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms._
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel
import util.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class UserAnswersCacheSpec extends BaseSpec with MockitoSugar with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  private val mockUserAnswers                   = mock[UserAnswers]
  private val mockpersonalDataDisclosureConsent = mock[Option[Boolean]]

  private val addressDetails =
    AddressViewModel(street = "street", city = "city", postcode = Some("postcode"), countryCode = "GB")

  private val subscriptionDetailsHolderService =
    new UserAnswersCache(mockSessionCache)

  protected override def beforeEach: Unit = {
    reset(mockSessionCache, mockUserAnswers)

    when(mockSessionCache.saveUserAnswers(any[UserAnswers])(any[HeaderCarrier]))
      .thenReturn(Future.successful(true))

    when(mockSessionCache.eori(any[HeaderCarrier]))
      .thenReturn(Future.successful(Some("GB122")))

    val existingHolder = UserAnswers(None, None, None, None, None, None, None)

    when(mockSessionCache.userAnswers(any[HeaderCarrier])).thenReturn(Future.successful(existingHolder))
    when(mockUserAnswers.personalDataDisclosureConsent).thenReturn(mockpersonalDataDisclosureConsent)

  }

  "Calling cacheAddressDetails" should {
    "save Address Details in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheAddressDetails(addressDetails), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.addressDetails shouldBe Some(addressDetails)

    }

    "should not save empty strings in postcode field" in {

      Await.result(
        subscriptionDetailsHolderService.cacheAddressDetails(addressDetails.copy(postcode = Some(""))),
        Duration.Inf
      )
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.addressDetails shouldBe Some(addressDetails.copy(postcode = None))
    }
  }

  "Calling cacheContactAddressDetails" should {
    "save Contact Address Details in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheContactAddressDetails(addressDetails), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.contactAddressDetails shouldBe Some(addressDetails)

    }

    "should not save empty strings in postcode field" in {

      Await.result(
        subscriptionDetailsHolderService.cacheContactAddressDetails(addressDetails.copy(postcode = Some(""))),
        Duration.Inf
      )
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.contactAddressDetails shouldBe Some(addressDetails.copy(postcode = None))
    }
  }

  "Calling cachePersonalDataDisclosureConsent" should {
    "save Personal Data Disclosure Consent in frontend cache" in {

      Await.result(
        subscriptionDetailsHolderService.cacheConsentToDisclosePersonalDetails(DisclosePersonalDetails.Yes),
        Duration.Inf
      )
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.personalDataDisclosureConsent shouldBe Some(true)
    }
  }
  "Calling cacheTradeWithNI" should {
    "save TradeWithNI in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheTradeWithNI(TradeWithNI.Yes), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.tradeWithNI shouldBe Some(true)
    }
  }
  "Calling cacheHaveEUEori" should {
    "save HaveEUEori in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheHaveEUEori(HaveEUEori.Yes), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.haveEUEori shouldBe Some(true)
    }
  }
  "Calling cacheHavePBEInNI" should {
    "save HavePBEInNI in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheHavePBEInNI(HavePBE.Yes), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.havePBEInNI shouldBe Some(true)
    }
  }
  "Calling cacheConfirmDetails" should {
    "save ConfirmDetails in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheConfirmDetails(ConfirmDetails.changeDetails), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.confirmDetails shouldBe Some("changeDetails")
    }
  }
  "Calling cacheSicCode" should {
    "save SicCode in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheSicCode("99978"), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.sicCode shouldBe Some("99978")

    }
  }

  "Calling cacheConfirmAddress" should {
    "save ConfirmAddress in frontend cache" in {

      Await.result(subscriptionDetailsHolderService.cacheConfirmAddress(PBEConfirmAddress.changeAddress), Duration.Inf)
      val requestCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      verify(mockSessionCache).saveUserAnswers(requestCaptor.capture())(ArgumentMatchers.eq(hc))
      val holder: UserAnswers = requestCaptor.getValue
      holder.confirmAddress shouldBe Some("changeAddress")
    }
  }

}
