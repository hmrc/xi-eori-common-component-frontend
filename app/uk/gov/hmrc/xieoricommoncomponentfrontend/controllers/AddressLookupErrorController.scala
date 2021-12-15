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

package uk.gov.hmrc.xieoricommoncomponentfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthAction
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{address_lookup_error, address_lookup_no_results}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupErrorController @Inject() (
  authAction: AuthAction,
  sessionCache: SessionCache,
  addressLookupErrorPage: address_lookup_error,
  addressLookupNoResultsPage: address_lookup_no_results,
  mcc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayErrorPage(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => _: LoggedInUserWithEnrolments =>
      Future.successful(Ok(addressLookupErrorPage(isPBEAddressLookupFailed = true)))
    }

  def displayContactAddressErrorPage(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => _: LoggedInUserWithEnrolments =>
      Future.successful(Ok(addressLookupErrorPage(isPBEAddressLookupFailed = false)))
    }

  def displayNoResultsPage(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => _: LoggedInUserWithEnrolments =>
      sessionCache.addressLookupParams.map {
        case Some(addressLookupParams) =>
          Ok(addressLookupNoResultsPage(addressLookupParams.postcode, isPBEAddresLookupFailed = true))
        case _ => Ok(addressLookupNoResultsPage("", isPBEAddresLookupFailed = true))
      }

    }

  def displayNoContactAddressResultsPage(): Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => _: LoggedInUserWithEnrolments =>
      sessionCache.contactAddressParams.map {
        case Some(addressLookupParams) =>
          Ok(addressLookupNoResultsPage(addressLookupParams.postcode, isPBEAddresLookupFailed = false))
        case _ => Redirect(routes.ContactAddressLookupController.onPageLoad())
      }
    }

}
