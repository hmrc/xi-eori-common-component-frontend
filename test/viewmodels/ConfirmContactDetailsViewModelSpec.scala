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

package viewmodels

import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail.ContactInformation
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.{ConfirmContactDetailsViewModel, ContactAddressViewModel}
import util.{BaseSpec, SpecData}

class ConfirmContactDetailsViewModelSpec extends BaseSpec with SpecData {

  "ConfirmContactDetailsViewModel apply" should {

    "create a view model successfully from a CaseInformation class" when {
      "all the fields are empty" in {
        val emptyContactInformation = ContactInformation(None, None, None, None, None, None, None)
        val viewModel               = ConfirmContactDetailsViewModel(emptyContactInformation)

        viewModel shouldBe ConfirmContactDetailsViewModel("", "", "", ContactAddressViewModel("", "", "", ""))
      }
      "all the fields provided" in {
        val viewModel = ConfirmContactDetailsViewModel(contactInformation)

        viewModel shouldBe ConfirmContactDetailsViewModel(
          "FirstName LastName",
          "1234567890",
          "test@example.com",
          ContactAddressViewModel("line 1", "Newcastle", "AA1 1AA", "GB")
        )
      }
    }
  }
}
