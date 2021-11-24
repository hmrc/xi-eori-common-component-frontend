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
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel

case class Address(addressLine1: String, addressLine2: Option[String], postalCode: Option[String], countryCode: String)

object Address {
  implicit val jsonFormat = Json.format[Address]

  def apply(
    addressLine1: String,
    addressLine2: Option[String],
    postalCode: Option[String],
    countryCode: String
  ): Address =
    new Address(addressLine1, addressLine2, postalCode.filter(_.nonEmpty), countryCode.toUpperCase()) {}

  def apply(address: AddressViewModel): Address =
    new Address(address.street, Some(address.city), address.postcode, address.countryCode) {}

}
