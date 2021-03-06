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
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.HavePBE

class HavePBESpec extends WordSpec with MustMatchers with ScalaCheckPropertyChecks with OptionValues {

  "HavePBE" must {

    "deserialise valid values" in {

      val gen = Gen.oneOf(HavePBE.values)

      forAll(gen) {
        havePBE =>
          JsString(havePBE.toString).validate[HavePBE].asOpt.value mustEqual havePBE
      }
    }

    "fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!HavePBE.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[HavePBE] mustEqual JsError("error.invalid")
      }
    }

    "serialise" in {

      val gen = Gen.oneOf(HavePBE.values)

      forAll(gen) {
        havePBE =>
          Json.toJson(havePBE)(HavePBE.writes) mustEqual JsString(havePBE.toString)
      }
    }
  }
}
