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

package models

import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ManualContactAddress
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel
import util.BaseSpec

class AddressViewModelSpec extends BaseSpec {

  val street      = "some building"
  val city        = "some street"
  val postcode    = "PC55 5AA"
  val countryCode = "EN"
  val line2       = "some area"
  val region      = "some town"

  val actualAddress =
    new ManualContactAddress(street, city, Some(postcode), countryCode, Some(line2), Some(region))

  val expectedAddress = new AddressViewModel(street, city, Some(postcode), countryCode, Some(line2), Some(region))

  "AddressViewModel" should {

    "Handle ManualContactAddress to AddressViewModel" in {
      AddressViewModel.apply(actualAddress) shouldEqual expectedAddress
    }

  }
}
