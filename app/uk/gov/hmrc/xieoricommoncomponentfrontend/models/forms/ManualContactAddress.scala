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

case class ManualContactAddress(
  line1: String,
  townOrCity: String,
  postcode: Option[String],
  country: String,
  line2: Option[String],
  regionOrState: Option[String]
) {}

object ManualContactAddress {
  implicit val format = Json.format[ManualContactAddress]

  def apply(addressViewModel: AddressViewModel): ManualContactAddress = {
    val line1    = addressViewModel.street
    val townCity = addressViewModel.city
    val postCode: String = addressViewModel.postcode match {
      case None            => ""
      case Some(p: String) => p
    }
    val country = addressViewModel.countryCode
    val line2 = addressViewModel.line2 match {
      case None            => ""
      case Some(p: String) => p
    }
    val regionState = addressViewModel.region match {
      case None            => ""
      case Some(p: String) => p
    }
    ManualContactAddress.apply(line1, townCity, Option(postCode), country, Option(line2), Option(regionState))
  }

}
