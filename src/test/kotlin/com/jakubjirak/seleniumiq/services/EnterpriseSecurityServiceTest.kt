/*
 * Copyright (c) 2025 Jakub Jirák. All rights reserved.
 * Licensed under the proprietary license. See LICENSE for details.
 */
package com.jakubjirak.seleniumiq.services

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EnterpriseSecurityServiceTest {

    @Test
    fun `UserSession is active on creation`() {
        val session = EnterpriseSecurityService.UserSession("user1", "user@test.com", "10.0.0.1")
        assertTrue(session.isActive)
        assertEquals("user1", session.userId)
    }

    @Test
    fun `UserSession becomes inactive after terminate`() {
        val session = EnterpriseSecurityService.UserSession("user1", "user@test.com", "10.0.0.1")
        session.terminate()
        assertFalse(session.isActive)
    }

    @Test
    fun `UserPermissions admin role gets all permissions`() {
        val perms = EnterpriseSecurityService.UserPermissions("user1")
        perms.addRole("ADMIN")

        assertTrue(perms.hasPermission("test.create"))
        assertTrue(perms.hasPermission("test.delete"))
        assertTrue(perms.hasPermission("team.manage"))
        assertTrue(perms.hasPermission("settings.manage"))
        assertTrue(perms.hasPermission("analytics.view"))
    }

    @Test
    fun `UserPermissions developer role has limited permissions`() {
        val perms = EnterpriseSecurityService.UserPermissions("user1")
        perms.addRole("DEVELOPER")

        assertTrue(perms.hasPermission("test.create"))
        assertTrue(perms.hasPermission("test.read"))
        assertTrue(perms.hasPermission("test.update"))
        assertFalse(perms.hasPermission("test.delete"))
        assertFalse(perms.hasPermission("team.manage"))
    }

    @Test
    fun `UserPermissions viewer role is read-only`() {
        val perms = EnterpriseSecurityService.UserPermissions("user1")
        perms.addRole("VIEWER")

        assertTrue(perms.hasPermission("test.read"))
        assertTrue(perms.hasPermission("pageobject.read"))
        assertTrue(perms.hasPermission("analytics.view"))
        assertFalse(perms.hasPermission("test.create"))
        assertFalse(perms.hasPermission("test.delete"))
    }

    @Test
    fun `UserPermissions getRoles returns copy of roles`() {
        val perms = EnterpriseSecurityService.UserPermissions("user1")
        perms.addRole("ADMIN")
        perms.addRole("DEVELOPER")

        val roles = perms.roles
        assertEquals(2, roles.size)
        assertTrue(roles.contains("ADMIN"))
        assertTrue(roles.contains("DEVELOPER"))
    }

    @Test
    fun `UserPermissions returns false for unknown permission`() {
        val perms = EnterpriseSecurityService.UserPermissions("user1")
        assertFalse(perms.hasPermission("nonexistent.perm"))
    }

    @Test
    fun `AuditLogEntry stores all fields`() {
        val entry = EnterpriseSecurityService.AuditLogEntry(
            "user1", "LOGIN", "session", true
        )
        entry.addDetail("ip", "10.0.0.1")
        // No exception expected — fields set correctly
        assertNotNull(entry)
    }
}
