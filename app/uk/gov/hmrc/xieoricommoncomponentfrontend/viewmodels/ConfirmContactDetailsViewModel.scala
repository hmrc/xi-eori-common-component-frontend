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

package uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels

import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail.ContactInformation

case class ConfirmContactDetailsViewModel(
  fullName: String,
  telephoneNumber: String,
  email: String,
  contactAddressViewModel: AddressViewModel
)

object ConfirmContactDetailsViewModel {

  def fromContactInformation(
    contactInformation: ContactInformation,
    addressViewModel: Option[AddressViewModel]
  ): Option[ConfirmContactDetailsViewModel] =
    for {
      personOfContact <- contactInformation.personOfContact if personOfContact.trim.nonEmpty
      telephoneNumber <- contactInformation.telephoneNumber if telephoneNumber.trim.nonEmpty
      emailAddress    <- contactInformation.emailAddress if emailAddress.trim.nonEmpty
      streetAndNumber <- contactInformation.streetAndNumber if streetAndNumber.trim.nonEmpty
    } yield {
      val address = addressViewModel.getOrElse(
        AddressViewModel(
          streetAndNumber,
          contactInformation.city.getOrElse(""),
          contactInformation.postalCode,
          contactInformation.countryCode.getOrElse(""),
          None,
          None
        )
      )
      ConfirmContactDetailsViewModel(personOfContact, telephoneNumber, emailAddress, address)
    }

}
