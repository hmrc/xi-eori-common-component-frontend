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

package uk.gov.hmrc.xieoricommoncomponentfrontend.models

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail.ContactInformation
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.cache.SubscriptionDisplayMongo

import java.time.LocalDate

case class SubscriptionInfoVatId(countryCode: Option[String], VATID: Option[String])

case class EstablishmentAddress(
  streetAndNumber: String,
  city: String,
  postalCode: Option[String] = None,
  countryCode: String
)

case class XiSubscription(XI_EORINo: String, XI_VATNumber: Option[String])

case class SubscriptionDisplayResponseDetail(
  EORINo: Option[String],
  CDSFullName: String,
  CDSEstablishmentAddress: EstablishmentAddress,
  contactInformation: Option[ContactInformation],
  VATIDs: Option[List[SubscriptionInfoVatId]],
  shortName: Option[String],
  dateOfEstablishment: Option[LocalDate] = None,
  XI_Subscription: Option[XiSubscription]
) {

  def toSubscriptionDisplayMongo: SubscriptionDisplayMongo = SubscriptionDisplayMongo(
    EORINo,
    CDSFullName,
    CDSEstablishmentAddress,
    contactInformation,
    VATIDs,
    shortName,
    dateOfEstablishment,
    XI_Subscription
  )

}

object SubscriptionDisplayResponseDetail {

  case class ContactInformation(
    personOfContact: Option[String],
    telephoneNumber: Option[String],
    emailAddress: Option[String],
    streetAndNumber: Option[String],
    city: Option[String],
    postalCode: Option[String],
    countryCode: Option[String],
  )

  implicit val addressFormat: OFormat[EstablishmentAddress]          = Json.format[EstablishmentAddress]
  implicit val vatFormat: OFormat[SubscriptionInfoVatId]             = Json.format[SubscriptionInfoVatId]
  implicit val xiSubscriptionFormat: OFormat[XiSubscription]         = Json.format[XiSubscription]
  implicit val contactInformationFormat: OFormat[ContactInformation] = Json.format[ContactInformation]

  implicit val subscriptionDisplayReads: Reads[SubscriptionDisplayResponseDetail] = (
    (JsPath \ "subscriptionDisplayResponse" \ "responseDetail" \ "EORINo").readNullable[String] and
      (JsPath \ "subscriptionDisplayResponse" \ "responseDetail" \ "CDSFullName").read[String] and
      (JsPath \ "subscriptionDisplayResponse" \ "responseDetail" \ "CDSEstablishmentAddress").read[
        EstablishmentAddress
      ] and
      (JsPath \ "subscriptionDisplayResponse" \ "responseDetail" \ "contactInformation").readNullable[
        ContactInformation
      ] and
      (JsPath \ "subscriptionDisplayResponse" \ "responseDetail" \ "VATIDs").readNullable[
        List[SubscriptionInfoVatId]
      ] and
      (JsPath \ "subscriptionDisplayResponse" \ "responseDetail" \ "shortName").readNullable[String] and
      (JsPath \ "subscriptionDisplayResponse" \ "responseDetail" \ "dateOfEstablishment").readNullable[LocalDate] and
      (JsPath \ "subscriptionDisplayResponse" \ "responseDetail" \ "XI_Subscription").readNullable[XiSubscription]
  )(SubscriptionDisplayResponseDetail.apply _)

  implicit val subscriptionDisplayWrites: OWrites[SubscriptionDisplayResponseDetail] =
    Json.writes[SubscriptionDisplayResponseDetail]

}
