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

package uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail.ContactInformation
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{EstablishmentAddress, SubscriptionInfoVatId, XiSubscription}

import java.time.LocalDate

case class SubscriptionDisplayMongo(
  EORINo: Option[String],
  CDSFullName: String,
  CDSEstablishmentAddress: EstablishmentAddress,
  contactInformation: Option[ContactInformation],
  VATIDs: Option[List[SubscriptionInfoVatId]],
  shortName: Option[String],
  dateOfEstablishment: Option[LocalDate] = None,
  XI_Subscription: Option[XiSubscription]
)

object SubscriptionDisplayMongo {
  implicit val addressFormat: OFormat[EstablishmentAddress]                      = Json.format[EstablishmentAddress]
  implicit val vatFormat: OFormat[SubscriptionInfoVatId]                         = Json.format[SubscriptionInfoVatId]
  implicit val contactInformationFormat: OFormat[ContactInformation]  = Json.format[ContactInformation]
  implicit val xiSubscriptionFormat: OFormat[XiSubscription]                     = Json.format[XiSubscription]
  implicit val subscriptionDisplayMongoFormat: OFormat[SubscriptionDisplayMongo] = Json.format[SubscriptionDisplayMongo]
}
