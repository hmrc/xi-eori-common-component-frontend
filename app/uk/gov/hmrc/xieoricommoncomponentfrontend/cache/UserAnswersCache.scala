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

package uk.gov.hmrc.xieoricommoncomponentfrontend.cache

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.UserAnswers
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.TradeWithNI.toBoolean
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.{
  ConfirmDetails,
  DisclosePersonalDetails,
  HaveEUEori,
  HavePBE,
  PBEConfirmAddress,
  TradeWithNI
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAnswersCache @Inject() (sessionCache: SessionCache)(implicit ec: ExecutionContext) {

  def saveUserAnswers(insertNewDetails: UserAnswers => UserAnswers)(implicit hc: HeaderCarrier): Future[Boolean] =
    sessionCache.userAnswers flatMap { details =>
      sessionCache.saveUserAnswers(insertNewDetails(details))
    }

  def getPersonalDataDisclosureConsent()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sessionCache.userAnswers map (_.personalDataDisclosureConsent)

  def cacheConsentToDisclosePersonalDetails(
    disclosePersonalDetails: DisclosePersonalDetails
  )(implicit hq: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(
      sd => sd.copy(personalDataDisclosureConsent = Some(DisclosePersonalDetails.toBoolean(disclosePersonalDetails)))
    )

  def cacheSicCode(sicCode: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(sd => sd.copy(sicCode = Some(sicCode)))

  def getSicCode()(implicit hc: HeaderCarrier): Future[Option[String]] =
    sessionCache.userAnswers map (_.sicCode)

  def getHavePBEInNI()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sessionCache.userAnswers map (_.havePBEInNI)

  def cacheHavePBEInNI(havePBE: HavePBE)(implicit hq: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(sd => sd.copy(havePBEInNI = Some(HavePBE.toBoolean(havePBE))))

  def cacheAddressDetails(address: AddressViewModel)(implicit hc: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(sd => sd.copy(addressDetails = Some(noneForEmptyPostcode(address))))

  def noneForEmptyPostcode(a: AddressViewModel) = a.copy(postcode = a.postcode.filter(_.nonEmpty))

  def cacheContactAddressDetails(address: AddressViewModel)(implicit hc: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(sd => sd.copy(contactAddressDetails = Some(noneForEmptyPostcode(address))))

  def getAddressDetails()(implicit hc: HeaderCarrier): Future[Option[AddressViewModel]] =
    sessionCache.userAnswers map (_.addressDetails)

  def getTradeWithInNI()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sessionCache.userAnswers map (_.tradeWithNI)

  def cacheTradeWithNI(tradeWithNI: TradeWithNI)(implicit hq: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(sd => sd.copy(tradeWithNI = Some(toBoolean(tradeWithNI))))

  def cacheHaveEUEori(haveEUEori: HaveEUEori)(implicit hc: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(sd => sd.copy(haveEUEori = Some(HaveEUEori.toBoolean(haveEUEori))))

  def getHaveEUEori()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sessionCache.userAnswers map (_.haveEUEori)

  def cacheConfirmDetails(confirmDetails: ConfirmDetails)(implicit hc: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(sd => sd.copy(confirmDetails = Some(ConfirmDetails.transformString(confirmDetails))))

  def getConfirmDetails()(implicit hc: HeaderCarrier): Future[Option[String]] =
    sessionCache.userAnswers map (_.confirmDetails)

  def cacheConfirmAddress(pbeConfirmAddress: PBEConfirmAddress)(implicit hc: HeaderCarrier): Future[Boolean] =
    saveUserAnswers(sd => sd.copy(confirmAddress = Some(PBEConfirmAddress.transformString(pbeConfirmAddress))))

  def getConfirmAddress()(implicit hc: HeaderCarrier): Future[Option[String]] =
    sessionCache.userAnswers map (_.confirmAddress)

}
