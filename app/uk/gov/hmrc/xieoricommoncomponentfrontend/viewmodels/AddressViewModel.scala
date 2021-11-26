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

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.AddressDetails

case class AddressViewModel(street: String, city: String, postcode: Option[String], countryCode: String) {
  val addressDetails: AddressDetails = AddressDetails(street, city, postcode, countryCode)
}

object AddressViewModel {
  implicit val jsonFormat: OFormat[AddressViewModel] = Json.format[AddressViewModel]

  def apply(street: String, city: String, postcode: Option[String], countryCode: String): AddressViewModel =
    new AddressViewModel(street.trim, city.trim, postcode.map(_.trim), countryCode)

}
