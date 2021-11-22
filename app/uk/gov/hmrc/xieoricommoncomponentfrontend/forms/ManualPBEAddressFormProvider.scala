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

import akka.parboiled2.RuleTrace.Optional
import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.mappings.Mappings
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.{ManualPBEAddress, StopOnFirstFail}

import javax.inject.Inject

class ManualPBEAddressFormProvider @Inject() extends Mappings {

  val postcodeRegex: String =
    "^(?i)(GIR 0AA)|((([A-Z][0-9][0-9]?)|(([A-Z][A-HJ-Y][0-9][0-9]?)|(([A-Z][0-9][A-Z])|([A-Z][A-HJ-Y][0-9]?[A-Z])))) ?[0-9][A-Z]{2})$"

  def apply(): Form[ManualPBEAddress] =
    Form(
      mapping(
        "line1" -> text("manual-pbe-address.line1.required").verifying(maxLength(70, "manual-pbe-address.line1.error")),
        "townorcity" -> text("manual-pbe-address.town.required").verifying(
          maxLength(35, "manual-pbe-address.town.error")
        ),
        "postcode" -> text("manual-pbe-address.postcode.required")
          .verifying(
            StopOnFirstFail(
              regexp(postcodeRegex, "manual-pbe-address.postcode.format.invalid"),
              btPostcode("manual-pbe-address.postcode.bt.format")
            )
          )
      )(ManualPBEAddress.apply)(ManualPBEAddress.unapply)
    )

}
