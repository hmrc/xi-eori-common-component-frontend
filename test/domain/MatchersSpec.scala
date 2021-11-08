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

package domain

import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain._
import util.BaseSpec

class MatchersSpec extends BaseSpec {

  val id   = java.util.UUID.randomUUID.toString
  val UTR  = Utr(id)
  val EORI = Eori(id)
  val NINO = Nino(id)

  val utrJson  = Json.parse(s"""{ "utr": "$id" }""")
  val eoriJson = Json.parse(s"""{ "eori": "$id" }""")
  val ninoJson = Json.parse(s"""{ "nino": "$id" }""")

  "UTR" should {
    passJsonTransformationCheck(UTR, utrJson)
  }

  "EORI" should {
    passJsonTransformationCheck(EORI, eoriJson)
  }

  "NINO" should {
    passJsonTransformationCheck(NINO, ninoJson)
  }

  private def passJsonTransformationCheck(customsId: CustomsId, expectedJson: JsValue) {
    "be marshalled" in {
      Json.toJson(customsId) shouldBe expectedJson
    }

    "be unmarshalled" in {
      Json.fromJson[CustomsId](expectedJson) shouldBe JsSuccess(customsId)
    }
  }

}
