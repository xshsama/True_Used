import defaultAvatarUrl from '@/assets/icons/user.svg'

const FAKE_AVATAR_PATTERNS = [
  'via.placeholder.com',
  'ui-avatars.com',
  'photo-1534528741775-53994a69daeb',
  'photo-1599566150163-29194dcaad36',
]

function isFakeAvatar(url) {
  return FAKE_AVATAR_PATTERNS.some((pattern) => url.includes(pattern))
}

export function resolveAvatar(...candidates) {
  for (const value of candidates) {
    if (typeof value !== 'string') continue
    const url = value.trim()
    if (!url) continue
    if (isFakeAvatar(url)) continue
    return url
  }
  return defaultAvatarUrl
}

export { defaultAvatarUrl }
