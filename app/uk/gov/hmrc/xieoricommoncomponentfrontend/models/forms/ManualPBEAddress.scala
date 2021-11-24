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

package uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms

import play.api.libs.json.Json
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel

case class ManualPBEAddress(line1: String, townorcity: String, postcode: String, country: Option[String]) {}

object ManualPBEAddress {
  implicit val format = Json.format[ManualPBEAddress]

  def fetchAddressDetail(addressViewModel: AddressViewModel): ManualPBEAddress = {
    val line1    = addressViewModel.street
    val townCity = addressViewModel.city
    val postCode: String = addressViewModel.postcode match {
      case None            => ""
      case Some(p: String) => p
    }
    val country = "GB"
    ManualPBEAddress.apply(line1, townCity, postCode, Option(country))
  }

  def toAddressModel(validPBEAddressParams: ManualPBEAddress): AddressViewModel = {
    val line1       = validPBEAddressParams.line1
    val townCity    = validPBEAddressParams.townorcity
    val postCode    = Some(validPBEAddressParams.postcode)
    val countryCode = "GB"
    AddressViewModel(line1, townCity, postCode, countryCode)
  }

}
