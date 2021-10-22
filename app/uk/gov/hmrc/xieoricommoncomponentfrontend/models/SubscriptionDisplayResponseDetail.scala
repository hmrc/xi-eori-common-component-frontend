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

import play.api.libs.json.Json

import java.time.{Clock, LocalDate, LocalDateTime, ZoneId}

case class SubscriptionInfoVatId(countryCode: Option[String], VATID: Option[String])

case class ContactInformation(
  personOfContact: Option[String] = None,
  sepCorrAddrIndicator: Option[Boolean] = None,
  streetAndNumber: Option[String] = None,
  city: Option[String] = None,
  postalCode: Option[String] = None,
  countryCode: Option[String] = None,
  telephoneNumber: Option[String] = None,
  faxNumber: Option[String] = None,
  emailAddress: Option[String] = None,
  emailVerificationTimestamp: Option[LocalDateTime] = Some(
    LocalDateTime.ofInstant(Clock.systemUTC().instant, ZoneId.of("Europe/London"))
  )
)

case class EstablishmentAddress(
  streetAndNumber: String,
  city: String,
  postalCode: Option[String] = None,
  countryCode: String
)

case class SubscriptionDisplayResponseDetail(
                                              EORINo: Option[String],
                                              CDSFullName: String,
                                              CDSEstablishmentAddress: EstablishmentAddress,
                                              establishmentInTheCustomsTerritoryOfTheUnion: Option[String],
                                              typeOfLegalEntity: Option[String],
                                              contactInformation: Option[ContactInformation],
                                              VATIDs: Option[List[SubscriptionInfoVatId]],
                                              thirdCountryUniqueIdentificationNumber: Option[List[String]],
                                              consentToDisclosureOfPersonalData: Option[String],
                                              shortName: Option[String],
                                              dateOfEstablishment: Option[LocalDate] = None,
                                              typeOfPerson: Option[String],
                                              principalEconomicActivity: Option[String]
                                            )


object SubscriptionDisplayResponseDetail {
  implicit val addressFormat             = Json.format[EstablishmentAddress]
  implicit val contactInformationFormat  = Json.format[ContactInformation]
  implicit val vatFormat                 = Json.format[SubscriptionInfoVatId]
  implicit val subscriptionDetailsFormat = Json.format[SubscriptionDisplayResponseDetail]
}