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
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.RegistrationDetails
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.{DisclosePersonalDetails, HavePBE, TradeWithNI}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserAnswersCache @Inject() (sessionCache: SessionCache)(implicit ec: ExecutionContext) {

  def saveRegistrationDetails(
    insertNewDetails: RegistrationDetails => RegistrationDetails
  )(implicit hc: HeaderCarrier): Future[Boolean] = sessionCache.registrationDetails flatMap { details =>
    sessionCache.saveRegistrationDetails(insertNewDetails(details))
  }

 /* def getRegistrationDetails()(implicit hc: HeaderCarrier)= sessionCache.registrationDetails.map{
    details => details.getOrElse(throw new IllegalStateException("No Registration Details Cached"))
  }*/

  def getPersonalDataDisclosureConsent()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sessionCache.registrationDetails map(_.personalDataDisclosureConsent)


  def cacheConsentToDisclosePersonalDetails(
    disclosePersonalDetails: DisclosePersonalDetails
  )(implicit hq: HeaderCarrier): Future[Boolean] =
        saveRegistrationDetails(sd => sd.copy(personalDataDisclosureConsent = Some(DisclosePersonalDetails.toBoolean(disclosePersonalDetails))))


  def cacheSicCode(sicCode: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    saveRegistrationDetails(sd => sd.copy(sicCode = Some(sicCode)))

  def getSicCode()(implicit hc: HeaderCarrier): Future[Option[String]] =
    sessionCache.registrationDetails map(_.sicCode)

  def getHavePBEInNI()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sessionCache.registrationDetails map(_.havePBEInNI)

  def cacheHavePBEInNI(havePBE: HavePBE)(implicit hq: HeaderCarrier): Future[Boolean] =
    saveRegistrationDetails(sd => sd.copy(havePBEInNI = Some(HavePBE.toBoolean(havePBE))))

  def cacheAddressDetails(address: AddressViewModel)(implicit hc: HeaderCarrier): Future[Boolean] = {
    def noneForEmptyPostcode(a: AddressViewModel) = a.copy(postcode = a.postcode.filter(_.nonEmpty))
    saveRegistrationDetails(sd => sd.copy(addressDetails = Some(noneForEmptyPostcode(address))))
  }

 /* def getTradeWithNI()(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    sessionCache.registrationDetails map {
      case Some(value) => value.tradeWithNI
      case _ => None
    }

  def cacheTradeWithNI(tradeWithNI: TradeWithNI)(implicit hq: HeaderCarrier): Future[Boolean] =
    saveRegistrationDetails(sd => sd.copy(tradeWithNI = Some(TradeWithNI.toBoolean(tradeWithNI))))
*/

}
