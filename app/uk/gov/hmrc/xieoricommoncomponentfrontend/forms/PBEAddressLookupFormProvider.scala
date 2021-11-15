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

package uk.gov.hmrc.xieoricommoncomponentfrontend.forms

import play.api.data.Form
import play.api.data.Forms.{mapping, optional}
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.mappings.Mappings
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.{PBEAddressLookup, StopOnFirstFail}

import javax.inject.Inject

class PBEAddressLookupFormProvider @Inject() extends Mappings {

  val postcodeRegex: String =
    "^(?i)(GIR 0AA)|((([A-Z][0-9][0-9]?)|(([A-Z][A-HJ-Y][0-9][0-9]?)|(([A-Z][0-9][A-Z])|([A-Z][A-HJ-Y][0-9]?[A-Z])))) ?[0-9][A-Z]{2})$"

  def apply(): Form[PBEAddressLookup] =
    Form(
      mapping(
        "postcode" -> text("pbe-address-lookup.postcode.required")
          .verifying(
            StopOnFirstFail(
              regexp(postcodeRegex, "pbe-address-lookup.postcode.format.invalid"),
              btPostcode("pbe-address-lookup.postcode.bt.format")
            )
          ),
        "line1" -> optional(text("").verifying(maxLength(35, "pbe-address-lookup.postcode.line1.error")))
      )(PBEAddressLookup.apply)(PBEAddressLookup.unapply)
    )

}
