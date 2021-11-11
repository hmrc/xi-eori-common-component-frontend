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

import play.api.data.Forms.text
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.data.{Form, Forms}
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.mappings.Mappings
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.SicCode

import javax.inject.Inject

class SicCodeFormProvider @Inject() extends Mappings {

  protected def validSicCode: Constraint[String] =
    Constraint("sic")({
      case s if s.trim.isEmpty               => Invalid(ValidationError("sicCode.error.required"))
      case s if !s.matches("[0-9]*")         => Invalid(ValidationError("sicCode.error.incorrect-format"))
      case s if s.length < 5 || s.length > 5 => Invalid(ValidationError("sicCode.error.length"))
      case _                                 => Valid
    })

  def apply(): Form[SicCode] = Form(
    Forms.mapping("sic" -> text("sicCode.error.required").verifying(validSicCode))(SicCode.apply)(SicCode.unapply)
  )

}
