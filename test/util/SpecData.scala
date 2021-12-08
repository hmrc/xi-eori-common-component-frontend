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

package util

import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, Eori, KeyValue}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail.ContactInformation
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{EstablishmentAddress, SubscriptionDisplayResponseDetail, SubscriptionInfoVatId, XiSubscription}

import java.time.LocalDate

trait SpecData {

  def groupEnrolment =
    List(EnrolmentResponse("HMRC-ATAR-ORG", "Activated", List(KeyValue("EORINumber", "GB123456463324"))))

  def existingEori: Option[Eori] = Some(Eori("XIE9XSDF10BCKEYAX"))

  def establishmentAddress: EstablishmentAddress = EstablishmentAddress(
    streetAndNumber = "line1",
    city = "City name",
    postalCode = Some("BT28 1AA"),
    countryCode = "GB"
  )

  def nonNiEstablishmentAddress: EstablishmentAddress = establishmentAddress.copy(postalCode = Some("AA1 1AA"))

  def contactInformation: ContactInformation =
    ContactInformation(
      personOfContact = Some("FirstName LastName"),
      telephoneNumber = Some("1234567890"),
      emailAddress = Some("test@example.com"),
      streetAndNumber = Some("line 1"),
      city = Some("Newcastle"),
      postalCode = Some("AA1 1AA"),
      countryCode = Some("GB")
    )

  def xiSubscription: XiSubscription = XiSubscription("XI8989989797", Some("999999"))

  def subscriptionDisplayResponse: SubscriptionDisplayResponseDetail = SubscriptionDisplayResponseDetail(
    EORINo = Some("GB123456789012"),
    CDSFullName = "FirstName LastName",
    CDSEstablishmentAddress = establishmentAddress,
    contactInformation = Some(contactInformation),
    VATIDs =     Some(List(SubscriptionInfoVatId(Some("GB"), Some("999999")), SubscriptionInfoVatId(Some("ES"), Some("888888")))),
    shortName = Some("Short Name"),
    dateOfEstablishment = Some(LocalDate.now()),
    XI_Subscription = Some(xiSubscription)
  )

  def nonNiSubscriptionDisplayResponse: SubscriptionDisplayResponseDetail = subscriptionDisplayResponse.copy(CDSEstablishmentAddress = nonNiEstablishmentAddress)
}
