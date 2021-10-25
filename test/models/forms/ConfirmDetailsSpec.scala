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

package models.forms

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmDetails

class ConfirmDetailsSpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  "ConfirmDetails" must {

    "deserialise valid values" in {

      val gen = Gen.oneOf(ConfirmDetails.values)

      forAll(gen) {
        tradeWithNI =>
          JsString(tradeWithNI.toString).validate[ConfirmDetails].asOpt.value mustEqual tradeWithNI
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ConfirmDetails.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[ConfirmDetails] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(ConfirmDetails.values)

      forAll(gen) {
        tradeWithNI =>
          Json.toJson(tradeWithNI)(ConfirmDetails.writes) mustEqual JsString(tradeWithNI.toString)
      }
    }
  }
}
