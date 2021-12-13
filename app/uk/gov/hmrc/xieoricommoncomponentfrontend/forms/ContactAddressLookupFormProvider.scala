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
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.{ContactAddressLookup, StopOnFirstFail}
import uk.gov.hmrc.xieoricommoncomponentfrontend.util.Constants

import javax.inject.Inject

class ContactAddressLookupFormProvider @Inject() extends Mappings with Constants {

  def apply(): Form[ContactAddressLookup] =
    Form(
      mapping(
        "postcode" -> text("contact-address-lookup.postcode.required")
          .verifying(
            StopOnFirstFail(regexp(Constants.postcodeRegex, "contact-address-lookup.postcode.format.invalid"))
          ),
        "line1" -> optional(text("").verifying(maxLength(35, "contact-address-lookup.postcode.line1.error")))
      )((postcode, line1) => ContactAddressLookup(postcode.toUpperCase, line1))(ContactAddressLookup.unapply)
    )

}
