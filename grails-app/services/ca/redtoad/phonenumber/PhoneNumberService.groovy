// -*- mode: Groovy; c-basic-offset: 4; tab-width: 4; indent-tabs-mode: nil; -*-

package ca.redtoad.phonenumber

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder

class PhoneNumberService {

    static transactional = false

    def grailsApplication
    def phoneNumberUtil

    String getDefaultRegion() {
        grailsApplication.config.grails.plugins.phonenumbers.defaultRegion ?: 'US'
    }

    Collection getAllowedRegions() {
        grailsApplication.config.grails.plugins.phonenumbers.defaultAllowedRegions ?: phoneNumberUtil.supportedRegions
    }

    boolean getStrict() {
        grailsApplication.config.grails.plugins.phonenumbers.defaultStrict ?: false
    }

    String format(String phoneNumber, PhoneNumberUtil.PhoneNumberFormat format = null) {
        if(phoneNumber == null) {
            return null
        }
        String result = null
        if(format == null) {
            format = PhoneNumberUtil.PhoneNumberFormat.E164
        }
        for(r in allowedRegions) {
            try {
                def phoneNumberInstance = phoneNumberUtil.parse(phoneNumber, r)
                if(phoneNumberUtil.isValidNumberForRegion(phoneNumberInstance, r)) {
                    result = phoneNumberUtil.format(phoneNumberInstance, format)
                }
            } catch (NumberParseException e) {
            }
            if(result) {
                return result
            }
        }
        return phoneNumber
    }

    Map geolocate(String phoneNumber, region = null, locale = null) {
        try {
            def geocoder = PhoneNumberOfflineGeocoder.instance
            def phoneNumberInstance = phoneNumberUtil.parse(phoneNumber, region ?: defaultRegion)
            [
                country: geocoder.getCountryNameForNumber(phoneNumberInstance, locale ?: Locale.default),
                description: geocoder.getDescriptionForNumber(phoneNumberInstance, locale ?: Locale.default)
            ]
        } catch (NumberParseException e) {
            [:]
        }
    }
}
